package com.example.fileshare.service;

import com.example.fileshare.dto.FileDTO;
import com.example.fileshare.dto.UserDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface FileService {
    FileDTO saveFile(MultipartFile file, UserDTO owner) throws Exception;
    Optional<FileDTO> findById(Long id);
    List<FileDTO> findByOwner(UserDTO owner);
    List<FileDTO> findSharedWithUser(UserDTO user);
    void deleteFile(Long id, UserDTO owner) throws Exception;
    byte[] downloadFile(Long id, UserDTO user) throws Exception;
    boolean canUserAccessFile(FileDTO file, UserDTO user);
}