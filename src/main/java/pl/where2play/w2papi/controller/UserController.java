package pl.where2play.w2papi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.where2play.w2papi.dto.UserDTO;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.service.UserService;

import java.util.List;

/**
 * Controller for user-related endpoints.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management API")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    /**
     * Get the current user's profile.
     *
     * @param userDetails the authenticated user details
     * @return the user DTO
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get the profile of the currently authenticated user")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Getting profile for user: {}", userDetails.getUsername());
        UserDTO userDTO = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Update the current user's profile.
     *
     * @param userDetails the authenticated user details
     * @param name the new name
     * @param pictureUrl the new picture URL
     * @return the updated user DTO
     */
    @PutMapping("/me")
    @Operation(summary = "Update profile", description = "Update the profile of the currently authenticated user")
    public ResponseEntity<UserDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String pictureUrl) {
        log.info("Updating profile for user: {}", userDetails.getUsername());
        User user = userService.getCurrentUser(userDetails.getUsername());
        UserDTO updatedUser = userService.updateProfile(user, name, pictureUrl);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete the current user's account.
     *
     * @param userDetails the authenticated user details
     * @return a success response
     */
    @DeleteMapping("/me")
    @Operation(summary = "Delete account", description = "Delete the account of the currently authenticated user")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Deleting account for user: {}", userDetails.getUsername());
        User user = userService.getCurrentUser(userDetails.getUsername());
        userService.deleteAccount(user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search for users by name or email.
     *
     * @param query the search query
     * @return the list of matching user DTOs
     */
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search for users by name or email")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String query) {
        log.info("Searching for users with query: {}", query);
        List<UserDTO> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }

    /**
     * Get a user by ID.
     *
     * @param id the user ID
     * @return the user DTO
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user", description = "Get a user by ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.info("Getting user with ID: {}", id);
        UserDTO userDTO = userService.getUserById(id);
        return ResponseEntity.ok(userDTO);
    }
}