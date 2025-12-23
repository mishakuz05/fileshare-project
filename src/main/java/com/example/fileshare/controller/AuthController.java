package com.example.fileshare.controller;

import com.example.fileshare.dto.UserDTO;
import com.example.fileshare.security.JwtService;
import com.example.fileshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username,
                                   @RequestParam String password) {
        try {
            UserDTO userDTO = userService.login(username, password);
            if (userDTO == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid login or password");
                return ResponseEntity.status(401).body(errorResponse);
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String token = jwtService.generateToken(userDetails);

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Login successful");
            response.put("username", userDTO.getUsername());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username,
                                      @RequestParam String password,
                                      @RequestParam String email) {
        try {
            if (userService.existsByUsername(username)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Username already exists");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (userService.existsByEmail(email)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Email already exists");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            userService.register(username, password, email);

            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}