package com.example.fileshare.service;

import com.example.fileshare.dto.UserDTO;
import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDTO register(String username, String password, String email);
    UserDTO login(String username, String password);
    Optional<UserDTO> findByUsername(String username);
    Optional<UserDTO> findById(Long id);
    List<UserDTO> findAll();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}