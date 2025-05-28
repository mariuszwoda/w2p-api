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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import pl.where2play.w2papi.dto.UserDTO;
import pl.where2play.w2papi.service.UserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDTO userDTO;
    private pl.where2play.w2papi.model.User user;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Set up test data
        userDTO = UserDTO.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .pictureUrl("https://example.com/picture.jpg")
                .provider("GOOGLE")
                .build();

        user = pl.where2play.w2papi.model.User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .pictureUrl("https://example.com/picture.jpg")
                .provider(pl.where2play.w2papi.model.User.AuthProvider.GOOGLE)
                .build();

        userDetails = User.withUsername("test@example.com")
                .password("")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    void testGetCurrentUser() {
        // Given
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDTO);

        // When
        ResponseEntity<UserDTO> response = userController.getCurrentUser(userDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("Test User", response.getBody().getName());
        assertEquals("https://example.com/picture.jpg", response.getBody().getPictureUrl());
        assertEquals("GOOGLE", response.getBody().getProvider());

        verify(userService).getUserByEmail("test@example.com");
    }

    @Test
    void testUpdateProfile() {
        // Given
        String name = "Updated Name";
        String pictureUrl = "https://example.com/new-picture.jpg";
        
        when(userService.getCurrentUser("test@example.com")).thenReturn(user);
        when(userService.updateProfile(eq(user), eq(name), eq(pictureUrl))).thenReturn(userDTO);

        // When
        ResponseEntity<UserDTO> response = userController.updateProfile(userDetails, name, pictureUrl);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("Test User", response.getBody().getName());
        assertEquals("https://example.com/picture.jpg", response.getBody().getPictureUrl());
        assertEquals("GOOGLE", response.getBody().getProvider());

        verify(userService).getCurrentUser("test@example.com");
        verify(userService).updateProfile(eq(user), eq(name), eq(pictureUrl));
    }

    @Test
    void testDeleteAccount() {
        // Given
        when(userService.getCurrentUser("test@example.com")).thenReturn(user);

        // When
        ResponseEntity<Void> response = userController.deleteAccount(userDetails);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(userService).getCurrentUser("test@example.com");
        verify(userService).deleteAccount(user);
    }

    @Test
    void testSearchUsers() {
        // Given
        String query = "test";
        
        UserDTO user2 = UserDTO.builder()
                .id(2L)
                .email("another@example.com")
                .name("Another User")
                .build();

        List<UserDTO> users = Arrays.asList(userDTO, user2);

        when(userService.searchUsers(query)).thenReturn(users);

        // When
        ResponseEntity<List<UserDTO>> response = userController.searchUsers(query);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("test@example.com", response.getBody().get(0).getEmail());
        assertEquals(2L, response.getBody().get(1).getId());
        assertEquals("another@example.com", response.getBody().get(1).getEmail());

        verify(userService).searchUsers(query);
    }

    @Test
    void testGetUserById() {
        // Given
        Long userId = 1L;
        when(userService.getUserById(userId)).thenReturn(userDTO);

        // When
        ResponseEntity<UserDTO> response = userController.getUserById(userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("Test User", response.getBody().getName());
        assertEquals("https://example.com/picture.jpg", response.getBody().getPictureUrl());
        assertEquals("GOOGLE", response.getBody().getProvider());

        verify(userService).getUserById(userId);
    }
}