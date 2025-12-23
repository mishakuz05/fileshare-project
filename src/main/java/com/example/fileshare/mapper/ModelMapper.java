package com.example.fileshare.mapper;

import com.example.fileshare.dto.FileDTO;
import com.example.fileshare.dto.FileShareDTO;
import com.example.fileshare.dto.UserDTO;
import com.example.fileshare.model.File;
import com.example.fileshare.model.FileShare;
import com.example.fileshare.model.User;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ModelMapper {
    public ModelMapper() {}

    public <D> D map(Object source, Class<D> destinationType) {
        Assert.notNull(source, "source");
        Assert.notNull(destinationType, "destinationType");
        if (source.getClass() == User.class && destinationType == UserDTO.class) {
            return toUserDTO((User) source);
        } else if (source.getClass() == UserDTO.class && destinationType == User.class) {
            return fromUserDTO((UserDTO) source);
        } else if (source.getClass() == File.class && destinationType == FileDTO.class) {
            return toFileDTO((File) source);
        } else if (source.getClass() == FileDTO.class && destinationType == File.class) {
            return fromFileDTO((FileDTO) source);
        } else if (source.getClass() == FileShare.class && destinationType == FileShareDTO.class) {
            return toFileShareDTO((FileShare) source);
        } else if (source.getClass() == FileShareDTO.class && destinationType == FileShare.class) {
            return fromFileShareDTO((FileShareDTO) source);
        }
        return null;
    }

    private <D> D toUserDTO(User user) {
        UserDTO userDTO = new UserDTO(user.getUsername(), user.getPassword(), user.getEmail());
        userDTO.setId(user.getId());
        return (D) userDTO;
    }

    private <D> D fromUserDTO(UserDTO userDTO) {
        User user = new User(userDTO.getUsername(), userDTO.getPassword(), userDTO.getEmail());
        user.setId(userDTO.getId());
        return (D) user;
    }

    private <D> D toFileDTO(File file) {
        FileDTO fileDTO = new FileDTO();
        fileDTO.setId(file.getId());
        fileDTO.setFilename(file.getFilename());
        fileDTO.setOriginalFilename(file.getOriginalFilename());
        fileDTO.setFilePath(file.getFilePath());
        fileDTO.setSize(file.getSize());
        fileDTO.setContentType(file.getContentType());
        fileDTO.setUploadDate(file.getUploadDate());
        UserDTO userDTO = toUserDTO(file.getOwner());
        fileDTO.setOwner(userDTO);
        return (D) fileDTO;
    }

    private <D> D fromFileDTO(FileDTO fileDTO) {
        File file = new File();
        file.setId(fileDTO.getId());
        file.setFilename(fileDTO.getFilename());
        file.setOriginalFilename(fileDTO.getOriginalFilename());
        file.setFilePath(fileDTO.getFilePath());
        file.setSize(fileDTO.getSize());
        file.setContentType(fileDTO.getContentType());
        file.setUploadDate(fileDTO.getUploadDate());
        var user = fromUserDTO(fileDTO.getOwner());
        file.setOwner((User) user);
        return (D) file;
    }

    private <D> D toFileShareDTO(FileShare fileShare) {
        FileShareDTO fileShareDTO = new FileShareDTO();
        fileShareDTO.setId(fileShare.getId());
        fileShareDTO.setFile(toFileDTO(fileShare.getFile()));
        fileShareDTO.setUser(toUserDTO(fileShare.getUser()));
        fileShareDTO.setSharedAt(fileShare.getSharedAt());
        fileShareDTO.setPermission(fileShare.getPermission());
        return (D) fileShareDTO;
    }

    private <D> D fromFileShareDTO(FileShareDTO fileShareDTO) {
        FileShare fileShare = new FileShare();
        fileShare.setId(fileShareDTO.getId());
        fileShare.setFile(fromFileDTO(fileShareDTO.getFile()));
        fileShare.setUser(fromUserDTO(fileShareDTO.getUser()));
        fileShare.setSharedAt(fileShareDTO.getSharedAt());
        fileShare.setPermission(fileShareDTO.getPermission());
        return (D) fileShare;
    }
}

