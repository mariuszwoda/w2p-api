package pl.where2play.w2papi.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

class UserTest {

    @Test
    void testUserCreation() {
        // Given
        String email = "test@example.com";
        String name = "Test User";
        String pictureUrl = "https://example.com/picture.jpg";
        User.AuthProvider provider = User.AuthProvider.GOOGLE;
        String providerId = "google123";

        // When
        User user = User.builder()
                .email(email)
                .name(name)
                .pictureUrl(pictureUrl)
                .provider(provider)
                .providerId(providerId)
                .build();

        // Then
        assertNotNull(user);
        assertEquals(email, user.getEmail());
        assertEquals(name, user.getName());
        assertEquals(pictureUrl, user.getPictureUrl());
        assertEquals(provider, user.getProvider());
        assertEquals(providerId, user.getProviderId());
        assertNotNull(user.getEvents());
        assertTrue(user.getEvents().isEmpty());
    }

    @Test
    void testUserEquality() {
        // Given
        User user1 = User.builder()
                .id(1L)
                .email("same@example.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .email("same@example.com")
                .build();

        User user3 = User.builder()
                .id(2L)
                .email("different@example.com")
                .build();

        // Then
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
    }
}