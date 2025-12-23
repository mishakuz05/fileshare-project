package com.example.fileshare.service.impl;

import com.example.fileshare.dto.FileDTO;
import com.example.fileshare.dto.UserDTO;
import com.example.fileshare.mapper.ModelMapper;
import com.example.fileshare.model.File;
import com.example.fileshare.model.User;
import com.example.fileshare.repository.FileRepository;
import com.example.fileshare.repository.FileShareRepository;
import com.example.fileshare.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileShareRepository fileShareRepository;

    @Autowired
    private ModelMapper mapper;

    @Override
    public FileDTO saveFile(MultipartFile file, UserDTO userDTO) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        File fileEntity = new File();
        fileEntity.setFilename(uniqueFilename);
        fileEntity.setOriginalFilename(originalFilename);
        fileEntity.setFilePath(filePath.toString());
        fileEntity.setSize(file.getSize());
        fileEntity.setContentType(file.getContentType());
        fileEntity.setUploadDate(LocalDateTime.now());
        User owner = mapper.map(userDTO, User.class);
        fileEntity.setOwner(owner);

        var fileDTO = fileRepository.save(fileEntity);
        return mapper.map(fileDTO, FileDTO.class);
    }

    @Override
    public Optional<FileDTO> findById(Long id) {
        Optional<File> fileOpt = fileRepository.findById(id);
        if (fileOpt.isEmpty()) return Optional.empty();
        FileDTO fileDTO = mapper.map(fileOpt.get(), FileDTO.class);
        return Optional.ofNullable(fileDTO);
    }

    @Override
    public List<FileDTO> findByOwner(UserDTO userDTO) {
        List<FileDTO> fileDTOList = new ArrayList<>();
        List<File> fileList = fileRepository.findByOwnerId(userDTO.getId());
        for (File file : fileList) {
            FileDTO fileDTO = mapper.map(file, FileDTO.class);
            fileDTOList.add(fileDTO);
        }
        return fileDTOList;
    }

    @Override
    public List<FileDTO> findSharedWithUser(UserDTO userDTO) {
        List<FileDTO> fileDTOList = new ArrayList<>();
        List<File> fileList = fileRepository.findSharedWithUserId(userDTO.getId());
        for (File file : fileList) {
            FileDTO fileDTO = mapper.map(file, FileDTO.class);
            fileDTOList.add(fileDTO);
        }
        return fileDTOList;
    }

    @Override
    public void deleteFile(Long id, UserDTO owner) throws Exception {
        Optional<File> fileOpt = fileRepository.findById(id);
        if (fileOpt.isPresent()) {
            File file = fileOpt.get();
            if (!file.getOwner().getId().equals(owner.getId())) {
                throw new SecurityException("You can only delete your own files");
            }

            Files.deleteIfExists(Paths.get(file.getFilePath()));
            fileRepository.delete(file);
        } else {
            throw new RuntimeException("The file does not exist");
        }
    }

    @Override
    public byte[] downloadFile(Long id, UserDTO userDTO) throws Exception {
        Optional<File> fileOpt = fileRepository.findById(id);
        if (fileOpt.isPresent()) {
            File file = fileOpt.get();
            FileDTO fileDTO = mapper.map(file, FileDTO.class);
            if (canUserAccessFile(fileDTO, userDTO)) {
                return Files.readAllBytes(Paths.get(file.getFilePath()));
            } else {
                throw new SecurityException("Access denied");
            }
        }
        throw new RuntimeException("File not found");
    }

    @Override
    public boolean canUserAccessFile(FileDTO fileDTO, UserDTO userDTO) {
        User user = mapper.map(userDTO, User.class);
        File file = mapper.map(fileDTO, File.class);
        return file.getOwner().getId().equals(user.getId()) ||
                fileShareRepository.existsByFileAndUser(file, user);
    }

    private String getFileExtension(String filename) {
        return filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf("."))
                : "";
    }
}