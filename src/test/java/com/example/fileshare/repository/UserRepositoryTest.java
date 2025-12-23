package com.example.fileshare.repository;

import com.example.fileshare.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Очистка перед каждым тестом
        entityManager.clear();

        // Создаем тестовых пользователей
        user1 = new User("john_doe", "password123", "john@example.com");
        entityManager.persist(user1);

        user2 = new User("jane_smith", "password456", "jane@example.com");
        entityManager.persist(user2);

        entityManager.flush();
    }

    @Test
    void contextLoads() {
        assertThat(userRepository).isNotNull();
        assertThat(entityManager).isNotNull();
    }

    @Test
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        // When
        Optional<User> result = userRepository.findByUsername("john_doe");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("john_doe");
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
        assertThat(result.get().getPassword()).isEqualTo("password123");
    }

    @Test
    void findByUsername_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<User> result = userRepository.findByUsername("nonexistent_user");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUsername_ShouldBeCaseSensitive() {
        // Given - разные регистры
        User user = new User("UserName", "password", "user@example.com");
        entityManager.persist(user);
        entityManager.flush();

        // When & Then - поиск должен быть регистрозависимым
        assertThat(userRepository.findByUsername("username")).isEmpty();
        assertThat(userRepository.findByUsername("UserName")).isPresent();
    }

    @Test
    void existsByUsername_WhenUsernameExists_ShouldReturnTrue() {
        // When
        Boolean result = userRepository.existsByUsername("john_doe");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void existsByUsername_WhenUsernameDoesNotExist_ShouldReturnFalse() {
        // When
        Boolean result = userRepository.existsByUsername("nonexistent_user");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void existsByUsername_ShouldBeCaseSensitive() {
        // When & Then - проверка должна быть регистрозависимой
        assertThat(userRepository.existsByUsername("JOHN_DOE")).isFalse();
        assertThat(userRepository.existsByUsername("john_doe")).isTrue();
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        // When
        Boolean result = userRepository.existsByEmail("john@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void existsByEmail_WhenEmailDoesNotExist_ShouldReturnFalse() {
        // When
        Boolean result = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void existsByEmail_ShouldBeCaseSensitive() {
        // When & Then - проверка должна быть регистрозависимой
        assertThat(userRepository.existsByEmail("JOHN@EXAMPLE.COM")).isFalse();
        assertThat(userRepository.existsByEmail("john@example.com")).isTrue();
    }

    @Test
    void save_ShouldPersistUserCorrectly() {
        // Given
        User newUser = new User("new_user", "new_password", "new@example.com");

        // When
        User savedUser = userRepository.save(newUser);
        entityManager.flush();
        entityManager.clear(); // Очищаем контекст для проверки из БД

        // Then
        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("new_user");
        assertThat(foundUser.getEmail()).isEqualTo("new@example.com");
        assertThat(foundUser.getPassword()).isEqualTo("new_password");
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // When
        var users = userRepository.findAll();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername)
                .containsExactlyInAnyOrder("john_doe", "jane_smith");
    }

    @Test
    void delete_ShouldRemoveUser() {
        // Given
        Long userId = user1.getId();

        // When
        userRepository.delete(user1);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(userRepository.findById(userId)).isEmpty();
        assertThat(userRepository.existsByUsername("john_doe")).isFalse();
    }

    @Test
    void update_ShouldModifyUser() {
        // Given
        User user = userRepository.findByUsername("john_doe").orElseThrow();
        user.setEmail("updated@example.com");
        user.setPassword("updated_password");

        // When
        User updatedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Then
        User foundUser = userRepository.findByUsername("john_doe").orElseThrow();
        assertThat(foundUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(foundUser.getPassword()).isEqualTo("updated_password");
    }

    @Test
    void findByUsername_WithMultipleUsers_ShouldReturnCorrectUser() {
        // Given - добавляем пользователя с похожим именем
        User user3 = new User("john_doe_jr", "password", "johnjr@example.com");
        entityManager.persist(user3);
        entityManager.flush();

        // When
        Optional<User> result = userRepository.findByUsername("john_doe");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("john_doe");
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
    }
}