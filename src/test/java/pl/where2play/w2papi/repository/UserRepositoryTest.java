package pl.where2play.w2papi.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pl.where2play.w2papi.model.User;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByEmail() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .name("Test User")
                .provider(User.AuthProvider.LOCAL)
                .build();
        
        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getName());
    }

    @Test
    void testExistsByEmail() {
        // Given
        User user = User.builder()
                .email("exists@example.com")
                .name("Exists User")
                .provider(User.AuthProvider.LOCAL)
                .build();
        
        entityManager.persist(user);
        entityManager.flush();

        // When & Then
        assertTrue(userRepository.existsByEmail("exists@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void testFindByProviderAndProviderId() {
        // Given
        UUID uuid = UUID.randomUUID();
        var mail = "google" + uuid + "@example.com";
        User user = User.builder()
                .email(mail)
                .name("Google User")
                .provider(User.AuthProvider.GOOGLE)
                .providerId("google123" + uuid)
                .build();
        
        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByProviderAndProviderId(
                User.AuthProvider.GOOGLE, "google123"+ uuid);

        // Then
        assertTrue(found.isPresent());
        assertEquals("Google User", found.get().getName());
        assertEquals(mail, found.get().getEmail());
    }
}