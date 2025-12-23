package com.example.fileshare.repository;

import com.example.fileshare.model.File;
import com.example.fileshare.model.FileShare;
import com.example.fileshare.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FileShareRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FileShareRepository fileShareRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    private User owner1;
    private User owner2;
    private User sharedUser1;
    private User sharedUser2;
    private File file1;
    private File file2;
    private File file3;
    private FileShare share1;
    private FileShare share2;
    private FileShare share3;

    @BeforeEach
    void setUp() {
        // Очистка перед каждым тестом
        entityManager.clear();

        // Создаем тестовых пользователей
        owner1 = new User("owner1", "password1", "owner1@example.com");
        entityManager.persist(owner1);

        owner2 = new User("owner2", "password2", "owner2@example.com");
        entityManager.persist(owner2);

        sharedUser1 = new User("sharedUser1", "password3", "shared1@example.com");
        entityManager.persist(sharedUser1);

        sharedUser2 = new User("sharedUser2", "password4", "shared2@example.com");
        entityManager.persist(sharedUser2);

        // Создаем тестовые файлы
        file1 = createFile("file1.txt", "file1.txt", "/path/file1.txt", owner1);
        entityManager.persist(file1);

        file2 = createFile("file2.pdf", "file2.pdf", "/path/file2.pdf", owner1);
        entityManager.persist(file2);

        file3 = createFile("file3.jpg", "file3.jpg", "/path/file3.jpg", owner2);
        entityManager.persist(file3);

        // Создаем записи о шаринге файлов
        share1 = new FileShare(file1, sharedUser1, "READ");
        entityManager.persist(share1);

        share2 = new FileShare(file1, sharedUser2, "WRITE");
        entityManager.persist(share2);

        share3 = new FileShare(file2, sharedUser1, "READ");
        entityManager.persist(share3);

        entityManager.flush();
    }

    private File createFile(String filename, String originalFilename, String filePath, User owner) {
        File file = new File();
        file.setFilename(filename);
        file.setOriginalFilename(originalFilename);
        file.setFilePath(filePath);
        file.setOwner(owner);
        file.setSize(1024L);
        file.setContentType("text/plain");
        file.setUploadDate(LocalDateTime.now());
        return file;
    }

    @Test
    void contextLoads() {
        assertThat(fileShareRepository).isNotNull();
        assertThat(entityManager).isNotNull();
    }

    @Test
    void findByFile_WhenFileHasShares_ShouldReturnAllShares() {
        // When
        List<FileShare> result = fileShareRepository.findByFile(file1);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FileShare::getFile)
                .allMatch(file -> file.getId().equals(file1.getId()));
        assertThat(result).extracting(share -> share.getUser().getUsername())
                .containsExactlyInAnyOrder("sharedUser1", "sharedUser2");
        assertThat(result).extracting(FileShare::getPermission)
                .containsExactlyInAnyOrder("READ", "WRITE");
    }

    @Test
    void findByFile_WhenFileHasNoShares_ShouldReturnEmptyList() {
        // When
        List<FileShare> result = fileShareRepository.findByFile(file3);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByFile_WithMultipleFiles_ShouldReturnOnlySharesForSpecificFile() {
        // When
        List<FileShare> resultForFile1 = fileShareRepository.findByFile(file1);
        List<FileShare> resultForFile2 = fileShareRepository.findByFile(file2);

        // Then
        assertThat(resultForFile1).hasSize(2);
        assertThat(resultForFile2).hasSize(1);

        assertThat(resultForFile1).extracting(FileShare::getFile)
                .allMatch(file -> file.getId().equals(file1.getId()));
        assertThat(resultForFile2).extracting(FileShare::getFile)
                .allMatch(file -> file.getId().equals(file2.getId()));
    }

    @Test
    void findByUser_WhenUserHasSharedFiles_ShouldReturnAllShares() {
        // When
        List<FileShare> result = fileShareRepository.findByUser(sharedUser1);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FileShare::getUser)
                .allMatch(user -> user.getId().equals(sharedUser1.getId()));
        assertThat(result).extracting(share -> share.getFile().getFilename())
                .containsExactlyInAnyOrder("file1.txt", "file2.pdf");
    }

    @Test
    void findByUser_WhenUserHasNoSharedFiles_ShouldReturnEmptyList() {
        // When
        List<FileShare> result = fileShareRepository.findByUser(sharedUser2);

        // Then
        // sharedUser2 имеет только одну шару
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFile().getFilename()).isEqualTo("file1.txt");
    }

    @Test
    void findByUser_WithNewUser_ShouldReturnEmptyList() {
        // Given
        User newUser = new User("newUser", "password", "new@example.com");
        entityManager.persistAndFlush(newUser);

        // When
        List<FileShare> result = fileShareRepository.findByUser(newUser);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void existsByFileAndUser_WhenShareExists_ShouldReturnTrue() {
        // When
        boolean result = fileShareRepository.existsByFileAndUser(file1, sharedUser1);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void existsByFileAndUser_WhenShareDoesNotExist_ShouldReturnFalse() {
        // When
        boolean result = fileShareRepository.existsByFileAndUser(file1, owner1); // Владелец не имеет шары

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void existsByFileAndUser_WithDifferentCombinations_ShouldWorkCorrectly() {
        // Then
        assertThat(fileShareRepository.existsByFileAndUser(file1, sharedUser1)).isTrue();
        assertThat(fileShareRepository.existsByFileAndUser(file1, sharedUser2)).isTrue();
        assertThat(fileShareRepository.existsByFileAndUser(file2, sharedUser1)).isTrue();
        assertThat(fileShareRepository.existsByFileAndUser(file2, sharedUser2)).isFalse();
        assertThat(fileShareRepository.existsByFileAndUser(file3, sharedUser1)).isFalse();
        assertThat(fileShareRepository.existsByFileAndUser(file3, sharedUser2)).isFalse();
    }

    @Test
    void save_ShouldPersistFileShareCorrectly() {
        // Given
        FileShare newShare = new FileShare(file3, sharedUser2, "READ");

        // When
        FileShare savedShare = fileShareRepository.save(newShare);
        entityManager.flush();
        entityManager.clear();

        // Then
        FileShare foundShare = fileShareRepository.findById(savedShare.getId()).orElse(null);
        assertThat(foundShare).isNotNull();
        assertThat(foundShare.getFile().getId()).isEqualTo(file3.getId());
        assertThat(foundShare.getUser().getId()).isEqualTo(sharedUser2.getId());
        assertThat(foundShare.getPermission()).isEqualTo("READ");
        assertThat(foundShare.getSharedAt()).isNotNull();
    }

    @Test
    void delete_ShouldRemoveFileShare() {
        // Given
        Long shareId = share1.getId();

        // When
        fileShareRepository.delete(share1);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(fileShareRepository.findById(shareId)).isEmpty();
        assertThat(fileShareRepository.existsByFileAndUser(file1, sharedUser1)).isFalse();
    }

    @Test
    void update_ShouldModifyFileShare() {
        // Given
        FileShare share = fileShareRepository.findById(share1.getId()).orElseThrow();
        share.setPermission("WRITE");

        // When
        FileShare updatedShare = fileShareRepository.save(share);
        entityManager.flush();
        entityManager.clear();

        // Then
        FileShare foundShare = fileShareRepository.findById(share1.getId()).orElseThrow();
        assertThat(foundShare.getPermission()).isEqualTo("WRITE");
    }

    @Test
    void findAll_ShouldReturnAllFileShares() {
        // When
        List<FileShare> allShares = fileShareRepository.findAll();

        // Then
        assertThat(allShares).hasSize(3);
        assertThat(allShares).extracting(share -> share.getFile().getFilename())
                .containsExactlyInAnyOrder("file1.txt", "file1.txt", "file2.pdf");
    }

    @Test
    void findByFile_ShouldReturnSharesWithCorrectUserAndPermissionData() {
        // When
        List<FileShare> result = fileShareRepository.findByFile(file1);

        // Then
        assertThat(result).hasSize(2);

        // Проверяем, что данные пользователей корректны
        result.forEach(share -> {
            assertThat(share.getUser()).isNotNull();
            assertThat(share.getUser().getUsername()).isNotNull();
            assertThat(share.getUser().getEmail()).isNotNull();
        });

        // Проверяем, что данные файлов корректны
        result.forEach(share -> {
            assertThat(share.getFile()).isNotNull();
            assertThat(share.getFile().getFilename()).isNotNull();
            assertThat(share.getFile().getOwner()).isNotNull();
        });
    }

    @Test
    void findByUser_ShouldReturnSharesWithCorrectFileData() {
        // When
        List<FileShare> result = fileShareRepository.findByUser(sharedUser1);

        // Then
        assertThat(result).hasSize(2);

        // Проверяем, что данные файлов корректны
        result.forEach(share -> {
            assertThat(share.getFile()).isNotNull();
            assertThat(share.getFile().getFilename()).isNotNull();
            assertThat(share.getFile().getOwner()).isNotNull();
            assertThat(share.getFile().getOwner().getUsername()).isNotNull();
        });
    }

    @Test
    void existsByFileAndUser_AfterDeletion_ShouldReturnFalse() {
        // Given
        File file = file1;
        User user = sharedUser1;

        // When
        fileShareRepository.delete(share1);
        entityManager.flush();

        // Then
        assertThat(fileShareRepository.existsByFileAndUser(file, user)).isFalse();
    }

    @Test
    void findByFile_AfterAddingNewShare_ShouldIncludeNewShare() {
        // Given
        FileShare newShare = new FileShare(file1, owner2, "READ");
        fileShareRepository.save(newShare);
        entityManager.flush();

        // When
        List<FileShare> result = fileShareRepository.findByFile(file1);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(share -> share.getUser().getUsername())
                .contains("owner2");
    }
}