package com.example.fileshare.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_shares")
public class FileShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    @JsonIgnore
    private File file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private LocalDateTime sharedAt;

    private String permission;

    public FileShare() {
        this.sharedAt = LocalDateTime.now();
    }

    public FileShare(File file, User user, String permission) {
        this();
        this.file = file;
        this.user = user;
        this.permission = permission;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public File getFile() { return file; }
    public void setFile(File file) { this.file = file; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getSharedAt() { return sharedAt; }
    public void setSharedAt(LocalDateTime sharedAt) { this.sharedAt = sharedAt; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
}
