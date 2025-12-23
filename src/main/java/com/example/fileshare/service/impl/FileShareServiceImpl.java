package com.example.fileshare.service.impl;

import com.example.fileshare.dto.FileDTO;
import com.example.fileshare.dto.FileShareDTO;
import com.example.fileshare.dto.UserDTO;
import com.example.fileshare.mapper.ModelMapper;
import com.example.fileshare.model.File;
import com.example.fileshare.model.FileShare;
import com.example.fileshare.model.User;
import com.example.fileshare.repository.FileShareRepository;
import com.example.fileshare.service.FileShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FileShareServiceImpl implements FileShareService {

    private final FileShareRepository fileShareRepository;

    public FileShareServiceImpl(FileShareRepository fileShareRepository) {
        this.fileShareRepository = fileShareRepository;
    }

    @Autowired
    private ModelMapper mapper;

    @Override
    public FileShareDTO shareFile(FileDTO file, UserDTO user, String permission) throws Exception {
        if (file.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Cannot share file with owner");
        }

        User targetUser = mapper.map(user, User.class);
        if (fileShareRepository.existsByFileAndUser(mapper.map(file, File.class), targetUser)) {
            throw new IllegalArgumentException("File already shared with this user");
        }

        File targetFile = mapper.map(file, File.class);
        FileShare fileShare = new FileShare(targetFile, targetUser, permission);
        return mapper.map(fileShareRepository.save(fileShare), FileShareDTO.class);
    }

    @Override
    public void revokeShare(Long shareId, UserDTO owner) throws Exception {
        FileShare fileShare = fileShareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Share not found"));

        if (!fileShare.getFile().getOwner().getId().equals(owner.getId())) {
            throw new SecurityException("Only file owner can revoke shares");
        }

        fileShareRepository.delete(fileShare);
    }

    @Override
    public List<FileShareDTO> getFileShares(FileDTO fileDTO) {
        List<FileShareDTO> fileShareDTOS = new ArrayList<>();
        List<FileShare> fileShares = fileShareRepository.findByFile(
                        mapper.map(fileDTO, File.class));
        for (FileShare fileShare : fileShares) {
            FileShareDTO fileShareDTO = mapper.map(fileShare, FileShareDTO.class);
            fileShareDTOS.add(fileShareDTO);
        }

        return fileShareDTOS;
    }

    @Override
    public List<FileShareDTO> getUserShares(UserDTO userDTO) {
        List<FileShare> fileShares = fileShareRepository.findByUser(
                mapper.map(userDTO, User.class)
        );
        List<FileShareDTO> fileShareDTOS = new ArrayList<>();
        for (FileShare fileShare : fileShares) {
            FileShareDTO fileShareDTO = mapper.map(fileShare, FileShareDTO.class);
            fileShareDTOS.add(fileShareDTO);
        }
        return fileShareDTOS;
    }

    @Override
    public boolean isFileSharedWithUser(FileDTO fileDTO, UserDTO userDTO) {
        File file = mapper.map(fileDTO, File.class);
        User user = mapper.map(userDTO, User.class);
        return fileShareRepository.existsByFileAndUser(file, user);
    }

    @Override
    public Optional<FileShareDTO> findById(Long shareId) {
        Optional<FileShare> fileShare = fileShareRepository.findById(shareId);
        if (fileShare.isEmpty()) return null;
        return Optional.ofNullable(mapper.map(fileShare.get(), FileShareDTO.class));
    }

    @Override
    public List<UserDTO> getUsersWithAccess(FileDTO fileDTO) {
        List<UserDTO> userDTOS = new ArrayList<>();
        File file = mapper.map(fileDTO, File.class);
        List<FileShare> fileShares = fileShareRepository.findByFile(file);
        for (FileShare fileShare : fileShares) {
            User user = fileShare.getUser();
            UserDTO userDTO = mapper.map(user, UserDTO.class);
            userDTOS.add(userDTO);
        }
        return userDTOS;
    }
}
