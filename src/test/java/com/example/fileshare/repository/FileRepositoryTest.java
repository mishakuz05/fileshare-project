package com.example.fileshare.repository;

import com.example.fileshare.model.File;
import com.example.fileshare.model.User;
import com.example.fileshare.model.FileShare;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FileRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FileRepository fileRepository;

    private User owner1;
    private User owner2;
    private User sharedUser;
    private File file1;
    private File file2;
    private File file3;

    @BeforeEach
    void setUp() {
        // Очистка перед каждым тестом
        entityManager.clear();

        // Создаем тестовых пользователей
        owner1 = new User("owner1", "password1", "owner1@example.com");
        entityManager.persist(owner1);

        owner2 = new User("owner2", "password2", "owner2@example.com");
        entityManager.persist(owner2);

        sharedUser = new User("sharedUser", "password3", "shared@example.com");
        entityManager.persist(sharedUser);

        // Создаем тестовые файлы
        file1 = createFile("file1.txt", "file1.txt", "/path/file1.txt", owner1);
        entityManager.persist(file1);

        file2 = createFile("file2.pdf", "file2.pdf", "/path/file2.pdf", owner1);
        entityManager.persist(file2);

        file3 = createFile("file3.jpg", "file3.jpg", "/path/file3.jpg", owner2);
        entityManager.persist(file3);

        // Создаем записи о шаринге файлов
        FileShare share1 = new FileShare(file1, sharedUser, "READ");
        entityManager.persist(share1);

        FileShare share2 = new FileShare(file3, sharedUser, "READ");
        entityManager.persist(share2);

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
        assertThat(fileRepository).isNotNull();
        assertThat(entityManager).isNotNull();
    }

    @Test
    void findByOwnerId_WhenOwnerExists_ShouldReturnFiles() {
        // When
        List<File> result = fileRepository.findByOwnerId(owner1.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(File::getFilename)
                .containsExactlyInAnyOrder("file1.txt", "file2.pdf");
        assertThat(result).allMatch(file -> file.getOwner().getId().equals(owner1.getId()));
    }

    @Test
    void findByOwnerId_WhenOwnerHasNoFiles_ShouldReturnEmptyList() {
        // Given
        User userWithoutFiles = new User("noFilesUser", "password", "nofiles@example.com");
        entityManager.persistAndFlush(userWithoutFiles);

        // When
        List<File> result = fileRepository.findByOwnerId(userWithoutFiles.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByOwnerId_WhenOwnerDoesNotExist_ShouldReturnEmptyList() {
        // When
        List<File> result = fileRepository.findByOwnerId(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByOwnerId_ShouldReturnFilesOrderedByIdAsc() {
        // When
        List<File> result = fileRepository.findByOwnerId(owner1.getId());

        // Then - проверяем сортировку по ID
        assertThat(result).isSortedAccordingTo((f1, f2) -> f1.getId().compareTo(f2.getId()));
    }

    @Test
    void findSharedWithUserId_WhenUserHasSharedFiles_ShouldReturnFiles() {
        // When
        List<File> result = fileRepository.findSharedWithUserId(sharedUser.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(File::getFilename)
                .containsExactlyInAnyOrder("file1.txt", "file3.jpg");

        // Проверяем, что файлы принадлежат разным владельцам
        assertThat(result).extracting(file -> file.getOwner().getId())
                .containsExactlyInAnyOrder(owner1.getId(), owner2.getId());
    }

    @Test
    void findSharedWithUserId_WhenUserHasNoSharedFiles_ShouldReturnEmptyList() {
        // Given
        User userWithoutShares = new User("noSharesUser", "password", "noshares@example.com");
        entityManager.persistAndFlush(userWithoutShares);

        // When
        List<File> result = fileRepository.findSharedWithUserId(userWithoutShares.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findSharedWithUserId_WhenUserDoesNotExist_ShouldReturnEmptyList() {
        // When
        List<File> result = fileRepository.findSharedWithUserId(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findSharedWithUserId_ShouldNotReturnUsersOwnFiles() {
        // Given - создаем файл, которым владеет sharedUser
        File sharedUserFile = createFile("ownFile.txt", "ownFile.txt", "/path/ownFile.txt", sharedUser);
        entityManager.persist(sharedUserFile);
        entityManager.flush();

        // When
        List<File> result = fileRepository.findSharedWithUserId(sharedUser.getId());

        // Then - файлы, где sharedUser является владельцем, не должны быть в результате
        assertThat(result).extracting(File::getFilename)
                .doesNotContain("ownFile.txt");
    }

    @Test
    void findSharedWithUserId_ShouldReturnOnlyFilesSharedWithSpecificUser() {
        // Given - создаем другого пользователя и расшариваем для него файл
        User anotherUser = new User("anotherUser", "password", "another@example.com");
        entityManager.persist(anotherUser);

        FileShare shareForAnother = new FileShare(file2, anotherUser, "READ");
        entityManager.persist(shareForAnother);
        entityManager.flush();

        // When - ищем файлы, расшаренные для sharedUser
        List<File> result = fileRepository.findSharedWithUserId(sharedUser.getId());

        // Then - должны получить только файлы, расшаренные именно для sharedUser
        assertThat(result).hasSize(2);
        assertThat(result).extracting(File::getFilename)
                .containsExactlyInAnyOrder("file1.txt", "file3.jpg")
                .doesNotContain("file2.pdf"); // file2.pdf расшарен для anotherUser, а не для sharedUser
    }
}