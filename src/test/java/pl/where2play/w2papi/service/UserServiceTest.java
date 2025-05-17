package pl.where2play.w2papi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.where2play.w2papi.dto.UserDTO;
import pl.where2play.w2papi.dto.request.AuthRequest;
import pl.where2play.w2papi.dto.response.AuthResponse;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.UserRepository;
import pl.where2play.w2papi.security.JwtTokenProvider;
import pl.where2play.w2papi.service.impl.UserServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .pictureUrl("https://example.com/picture.jpg")
                .provider(User.AuthProvider.GOOGLE)
                .providerId("google123")
                .build();

        authRequest = AuthRequest.builder()
                .token("test-token")
                .provider("GOOGLE")
                .build();
    }

    @Test
    void testAuthenticate() {
        // Given
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("jwt-token");
        when(userRepository.findByProviderAndProviderId(any(User.AuthProvider.class), anyString()))
                .thenReturn(Optional.of(user));

        // When
        AuthResponse response = userService.authenticate(authRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertTrue(response.getExpiresIn() > 0);
        assertNotNull(response.getUser());
        assertEquals(user.getId(), response.getUser().getId());
        assertEquals(user.getEmail(), response.getUser().getEmail());
        assertEquals(user.getName(), response.getUser().getName());

        verify(jwtTokenProvider).generateToken(anyString());
        verify(userRepository).findByProviderAndProviderId(any(User.AuthProvider.class), anyString());
    }

    @Test
    void testAuthenticateNewUser() {
        // Given
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("jwt-token");
        when(userRepository.findByProviderAndProviderId(any(User.AuthProvider.class), anyString()))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        AuthResponse response = userService.authenticate(authRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertNotNull(response.getUser());

        verify(jwtTokenProvider).generateToken(anyString());
        verify(userRepository).findByProviderAndProviderId(any(User.AuthProvider.class), anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testGetCurrentUser() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When
        User result = userService.getCurrentUser("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getEmail(), result.getEmail());

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testGetUserById() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        UserDTO result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getName(), result.getName());

        verify(userRepository).findById(1L);
    }

    @Test
    void testGetUserByEmail() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When
        UserDTO result = userService.getUserByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getName(), result.getName());

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testSearchUsers() {
        // Given
        User user2 = User.builder()
                .id(2L)
                .email("another@example.com")
                .name("Another User")
                .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));

        // When
        List<UserDTO> results = userService.searchUsers("test");

        // Then
        assertEquals(2, results.size());
        assertEquals(user.getId(), results.get(0).getId());
        assertEquals(user2.getId(), results.get(1).getId());

        verify(userRepository).findAll();
    }

    @Test
    void testUpdateProfile() {
        // Given
        String newName = "Updated Name";
        String newPictureUrl = "https://example.com/new-picture.jpg";

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserDTO result = userService.updateProfile(user, newName, newPictureUrl);

        // Then
        assertNotNull(result);
        assertEquals(newName, result.getName());
        assertEquals(newPictureUrl, result.getPictureUrl());

        verify(userRepository).save(user);
    }

    @Test
    void testDeleteAccount() {
        // When
        userService.deleteAccount(user);

        // Then
        verify(userRepository).delete(user);
    }

    @Test
    void testFindByProviderAndProviderId() {
        // Given
        when(userRepository.findByProviderAndProviderId(User.AuthProvider.GOOGLE, "google123"))
                .thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findByProviderAndProviderId(User.AuthProvider.GOOGLE, "google123");

        // Then
        assertTrue(result.isPresent());
        assertEquals(user.getId(), result.get().getId());

        verify(userRepository).findByProviderAndProviderId(User.AuthProvider.GOOGLE, "google123");
    }
}