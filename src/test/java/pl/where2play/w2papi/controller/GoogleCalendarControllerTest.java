package pl.where2play.w2papi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.service.GoogleCalendarService;
import pl.where2play.w2papi.service.UserService;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleCalendarControllerTest {

    @Mock
    private GoogleCalendarService googleCalendarService;

    @Mock
    private UserService userService;

    @InjectMocks
    private GoogleCalendarController googleCalendarController;

    private User user;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Set up test data
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .provider(User.AuthProvider.GOOGLE)
                .build();

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("test@example.com")
                .password("")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    void testGetAuthorizationUrl() {
        // Given
        String authUrl = "https://accounts.google.com/o/oauth2/auth?client_id=test-client-id";
        when(googleCalendarService.getAuthorizationUrl()).thenReturn(authUrl);

        // When
        ResponseEntity<Map<String, String>> response = googleCalendarController.getAuthorizationUrl();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(authUrl, response.getBody().get("authorizationUrl"));

        verify(googleCalendarService).getAuthorizationUrl();
    }

    @Test
    void testExchangeCodeForToken() {
        // Given
        String code = "test-code";
        when(userService.getCurrentUser("test@example.com")).thenReturn(user);
        when(googleCalendarService.exchangeCodeForToken(eq(code), eq(user))).thenReturn(true);

        // When
        ResponseEntity<Map<String, Boolean>> response = googleCalendarController.exchangeCodeForToken(code, userDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("success"));

        verify(userService).getCurrentUser("test@example.com");
        verify(googleCalendarService).exchangeCodeForToken(eq(code), eq(user));
    }

    @Test
    void testCheckAuthorizationStatus() {
        // Given
        when(userService.getCurrentUser("test@example.com")).thenReturn(user);
        when(googleCalendarService.isAuthorized(user)).thenReturn(true);

        // When
        ResponseEntity<Map<String, Boolean>> response = googleCalendarController.checkAuthorizationStatus(userDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("authorized"));

        verify(userService).getCurrentUser("test@example.com");
        verify(googleCalendarService).isAuthorized(user);
    }
}