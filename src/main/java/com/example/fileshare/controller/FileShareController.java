package com.example.fileshare.controller;

import com.example.fileshare.dto.FileDTO;
import com.example.fileshare.dto.FileShareDTO;
import com.example.fileshare.dto.UserDTO;
import com.example.fileshare.service.FileService;
import com.example.fileshare.service.FileShareService;
import com.example.fileshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/share")
public class FileShareController {

    @Autowired
    private FileShareService fileShareService;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    private UserDTO getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @PostMapping("/{fileId}")
    public ResponseEntity<?> shareFile(@PathVariable Long fileId,
                                       @RequestParam Long userId,
                                       @RequestParam String permission) {
        try {
            UserDTO currentUser = getCurrentUser();
            FileDTO file = fileService.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            // Check if current user is the file owner
            if (!file.getOwner().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only share your own files"));
            }

            UserDTO targetUser = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            fileShareService.shareFile(file, targetUser, permission);

            Map<String, String> response = new HashMap<>();
            response.put("message", "File shared successfully");
            response.put("fileId", fileId.toString());
            response.put("sharedWith", targetUser.getUsername());
            response.put("permission", permission);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Share failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/{shareId}")
    public ResponseEntity<?> revokeShare(@PathVariable Long shareId) {
        try {
            UserDTO currentUser = getCurrentUser();
            fileShareService.revokeShare(shareId, currentUser);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Share revoked successfully");
            response.put("shareId", shareId.toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Revoke failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<?> getFileShares(@PathVariable Long fileId) {
        try {
            UserDTO currentUser = getCurrentUser();
            FileDTO file = fileService.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            // Check if user has access to the file
            if (!fileService.canUserAccessFile(file, currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied to file"));
            }

            List<FileShareDTO> shares = fileShareService.getFileShares(file);

            Map<String, Object> response = new HashMap<>();
            response.put("fileId", fileId);
            response.put("filename", file.getOriginalFilename());
            response.put("shares", shares);
            response.put("totalShares", shares.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get shares: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/my-shares")
    public ResponseEntity<?> getMySharedFiles() {
        try {
            UserDTO currentUser = getCurrentUser();
            List<FileShareDTO> shares = fileShareService.getUserShares(currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("sharedFiles", shares);
            response.put("totalSharedFiles", shares.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get shared files: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getShareableUsers() {
        try {
            UserDTO currentUser = getCurrentUser();
            List<UserDTO> allUsers = userService.findAll();

            List<UserDTO> shareableUsers = allUsers.stream()
                    .filter(user -> !user.getId().equals(currentUser.getId()))
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("users", shareableUsers);
            response.put("totalUsers", shareableUsers.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get users: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}