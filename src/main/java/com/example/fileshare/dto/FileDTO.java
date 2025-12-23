package com.example.fileshare.dto;

import com.example.fileshare.model.FileShare;
import com.example.fileshare.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileDTO {
    private Long id;
    private String filename;
    private String originalFilename;
    private String filePath;
    private Long size;
    private String contentType;
    private LocalDateTime uploadDate;
    private UserDTO owner;
    //private String owner_name;
    //private List<Long> shares_id = new ArrayList<>();

    public FileDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
//    public String getOwner_name() { return owner_name; }
//    public void setOwner_Name(String owner_name) { this.owner_name = owner_name; }

    public UserDTO getOwner() {
        return owner;
    }

    public void setOwner(UserDTO userDTO) {
        this.owner = userDTO;
    }

    //public List<FileShare> getShares() { return shares; }
    //public void setShares(List<FileShare> shares) { this.shares = shares; }
}

