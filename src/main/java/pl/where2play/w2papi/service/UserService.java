package pl.where2play.w2papi.service;

import pl.where2play.w2papi.dto.UserDTO;
import pl.where2play.w2papi.dto.request.AuthRequest;
import pl.where2play.w2papi.dto.response.AuthResponse;
import pl.where2play.w2papi.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing users.
 */
public interface UserService {

    /**
     * Authenticate a user with an OAuth2 token.
     *
     * @param authRequest the authentication request
     * @return the authentication response
     */
    AuthResponse authenticate(AuthRequest authRequest);

    /**
     * Get the current authenticated user.
     *
     * @param email the user's email
     * @return the user
     */
    User getCurrentUser(String email);

    /**
     * Get a user by ID.
     *
     * @param id the user ID
     * @return the user DTO
     */
    UserDTO getUserById(Long id);

    /**
     * Get a user by email.
     *
     * @param email the user email
     * @return the user DTO
     */
    UserDTO getUserByEmail(String email);

    /**
     * Search for users by name or email.
     *
     * @param query the search query
     * @return the list of user DTOs
     */
    List<UserDTO> searchUsers(String query);

    /**
     * Update a user's profile.
     *
     * @param user the user to update
     * @param name the new name
     * @param pictureUrl the new picture URL
     * @return the updated user DTO
     */
    UserDTO updateProfile(User user, String name, String pictureUrl);

    /**
     * Delete a user account.
     *
     * @param user the user to delete
     */
    void deleteAccount(User user);

    /**
     * Find a user by provider and provider ID.
     *
     * @param provider the authentication provider
     * @param providerId the provider-specific user ID
     * @return the user, if found
     */
    Optional<User> findByProviderAndProviderId(User.AuthProvider provider, String providerId);
}