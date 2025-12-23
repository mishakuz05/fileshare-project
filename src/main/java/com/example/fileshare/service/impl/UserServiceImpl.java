package com.example.fileshare.service.impl;

import com.example.fileshare.dto.UserDTO;
import com.example.fileshare.mapper.ModelMapper;
import com.example.fileshare.model.User;
import com.example.fileshare.repository.UserRepository;
import com.example.fileshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper mapper;

    @Override
    public UserDTO register(String username, String password, String email) {
        if (userRepository.existsByUsername(username) ||
        userRepository.existsByEmail(email)) {
            return null;
        }
        User user = new User(username, passwordEncoder.encode(password), email);
        return mapper.map(userRepository.save(user), UserDTO.class);
    }

    @Override
    public UserDTO login(String username, String password) {
        var userOpt =  userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return null;
        var user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) return null;
        return mapper.map(user, UserDTO.class);
    }

    @Override
    public Optional<UserDTO> findByUsername(String username) {
        Optional<User> user =  userRepository.findByUsername(username);
        if (user.isEmpty()) return Optional.empty();
        return Optional.ofNullable(mapper.map(user.get(), UserDTO.class));
    }

    @Override
    public Optional<UserDTO> findById(Long id) {
        Optional<User> user =  userRepository.findById(id);
        if (user.isEmpty()) return Optional.empty();
        return Optional.ofNullable(mapper.map(user.get(), UserDTO.class));
    }

    @Override
    public List<UserDTO> findAll() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOS = new ArrayList<>();
        for (User user : users) {
            UserDTO userDTO = mapper.map(user, UserDTO.class);
            userDTOS.add(userDTO);
        }
        return userDTOS;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}