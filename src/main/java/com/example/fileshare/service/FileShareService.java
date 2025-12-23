package com.example.fileshare.service;

import com.example.fileshare.dto.FileDTO;
import com.example.fileshare.dto.FileShareDTO;
import com.example.fileshare.dto.UserDTO;
//import com.example.fileshare.model.File;
//import com.example.fileshare.model.FileShare;
//import com.example.fileshare.model.User;

import java.util.List;
import java.util.Optional;

public interface FileShareService {
    FileShareDTO shareFile(FileDTO file, UserDTO user, String permission) throws Exception;

    void revokeShare(Long shareId, UserDTO owner) throws Exception;

    List<FileShareDTO> getFileShares(FileDTO fileDTO);

    List<FileShareDTO> getUserShares(UserDTO userDTO);

    boolean isFileSharedWithUser(FileDTO file, UserDTO user);

    Optional<FileShareDTO> findById(Long shareId);

    List<UserDTO> getUsersWithAccess(FileDTO fileDTO);
}