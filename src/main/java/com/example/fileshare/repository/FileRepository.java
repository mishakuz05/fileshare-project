package com.example.fileshare.repository;

import com.example.fileshare.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    @Query("SELECT f FROM File f WHERE f.owner.id = :user_id ORDER BY f.id ASC")
    List<File> findByOwnerId(@Param("user_id") Long user_id);

    @Query("SELECT f FROM File f JOIN f.shares fs WHERE fs.user.id = :user_id")
    List<File> findSharedWithUserId(@Param("user_id") Long user_id);
}