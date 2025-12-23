package com.example.fileshare.service.impl;

import com.example.fileshare.dto.UserDTO;
import com.example.fileshare.mapper.ModelMapper;
import com.example.fileshare.model.User;
import com.example.fileshare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;
    private final String username = "testuser";
    private final String password = "password123";
    private final String encodedPassword = "encodedPassword123";
    private final String email = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = new User(username, encodedPassword, email);
        testUser.setId(1L);

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setUsername(username);
        testUserDTO.setEmail(email);
    }

    @Test
    void register_Success() {
        // Arrange
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(mapper.map(testUser, UserDTO.class)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.register(username, password, email);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());

        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
        verify(mapper).map(testUser, UserDTO.class);
    }

    @Test
    void register_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act
        UserDTO result = userService.register(username, password, email);

        // Assert
        assertNull(result);
        verify(userRepository).existsByUsername(username);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act
        UserDTO result = userService.register(username, password, email);

        // Assert
        assertNull(result);
        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(mapper.map(testUser, UserDTO.class)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.login(username, password);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(password, encodedPassword);
        verify(mapper).map(testUser, UserDTO.class);
    }

    @Test
    void login_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        UserDTO result = userService.login(username, password);

        // Assert
        assertNull(result);
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_WrongPassword() {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        // Act
        UserDTO result = userService.login(username, password);

        // Assert
        assertNull(result);
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(password, encodedPassword);
        verify(mapper, never()).map(any(User.class), eq(UserDTO.class));
    }

    @Test
    void findByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mapper.map(testUser, UserDTO.class)).thenReturn(testUserDTO);

        // Act
        Optional<UserDTO> result = userService.findByUsername(username);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        verify(userRepository).findByUsername(username);
        verify(mapper).map(testUser, UserDTO.class);
    }

    @Test
    void findByUsername_NotFound() {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        Optional<UserDTO> result = userService.findByUsername(username);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository).findByUsername(username);
        verify(mapper, never()).map(any(User.class), eq(UserDTO.class));
    }

    @Test
    void findById_Success() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(mapper.map(testUser, UserDTO.class)).thenReturn(testUserDTO);

        // Act
        Optional<UserDTO> result = userService.findById(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        verify(userRepository).findById(userId);
        verify(mapper).map(testUser, UserDTO.class);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        Optional<UserDTO> result = userService.findById(userId);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository).findById(userId);
        verify(mapper, never()).map(any(User.class), eq(UserDTO.class));
    }

    @Test
    void findAll() {
        // Arrange
        List<User> users = List.of(testUser, new User("user2", "pass2", "email2@example.com"));
        List<UserDTO> userDTOs = List.of(testUserDTO, new UserDTO());

        when(userRepository.findAll()).thenReturn(users);
        when(mapper.map(any(User.class), eq(UserDTO.class)))
                .thenReturn(testUserDTO)
                .thenReturn(userDTOs.get(1));

        // Act
        List<UserDTO> result = userService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAll();
        verify(mapper, times(2)).map(any(User.class), eq(UserDTO.class));
    }

    @Test
    void findAll_EmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());

        // Act
        List<UserDTO> result = userService.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
        verify(mapper, never()).map(any(User.class), eq(UserDTO.class));
    }

    @Test
    void existsByUsername() {
        // Arrange
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act
        boolean result = userService.existsByUsername(username);

        // Assert
        assertTrue(result);
        verify(userRepository).existsByUsername(username);
    }

    @Test
    void existsByEmail() {
        // Arrange
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act
        boolean result = userService.existsByEmail(email);

        // Assert
        assertTrue(result);
        verify(userRepository).existsByEmail(email);
    }
}