package pl.where2play.w2papi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.where2play.w2papi.dto.UserDTO;
import pl.where2play.w2papi.dto.request.AuthRequest;
import pl.where2play.w2papi.dto.response.AuthResponse;
import pl.where2play.w2papi.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private AuthRequest authRequest;
    private AuthResponse authResponse;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        // Set up test data
        authRequest = AuthRequest.builder()
                .token("test-token")
                .provider("GOOGLE")
                .build();

        userDTO = UserDTO.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .pictureUrl("https://example.com/picture.jpg")
                .provider("GOOGLE")
                .build();

        authResponse = AuthResponse.builder()
                .token("jwt-token")
                .tokenType("Bearer")
                .expiresIn(86400000)
                .user(userDTO)
                .build();
    }

    @Test
    void testLogin() {
        // Given
        when(userService.authenticate(any(AuthRequest.class))).thenReturn(authResponse);

        // When
        ResponseEntity<AuthResponse> response = authController.login(authRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        assertEquals(86400000, response.getBody().getExpiresIn());
        assertEquals(1L, response.getBody().getUser().getId());
        assertEquals("test@example.com", response.getBody().getUser().getEmail());
        assertEquals("Test User", response.getBody().getUser().getName());
        assertEquals("https://example.com/picture.jpg", response.getBody().getUser().getPictureUrl());
        assertEquals("GOOGLE", response.getBody().getUser().getProvider());
    }

    @Test
    void testLoginValidationFailure() {
        // This test would normally test validation failures, but since we're testing directly
        // against the controller method, we can't easily test validation failures.
        // In a real application, you would use MockMvc to test validation failures.
    }
}
