package com.example.fileshare.repository;

import com.example.fileshare.model.File;
import com.example.fileshare.model.FileShare;
import com.example.fileshare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileShareRepository extends JpaRepository<FileShare, Long> {
    List<FileShare> findByFile(File file);

    List<FileShare> findByUser(User user);

    boolean existsByFileAndUser(File file, User user);
}