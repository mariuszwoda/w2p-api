package pl.where2play.w2papi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.where2play.w2papi.dto.UserDTO;
import pl.where2play.w2papi.dto.request.AuthRequest;
import pl.where2play.w2papi.dto.response.AuthResponse;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.UserRepository;
import pl.where2play.w2papi.security.JwtTokenProvider;
import pl.where2play.w2papi.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the UserService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse authenticate(AuthRequest authRequest) {
        log.info("Authenticating user with provider: {}", authRequest.getProvider());

        // In a real implementation, this would validate the token with the provider
        // and extract user information from the token

        // For demonstration purposes, we'll create a mock implementation
        User.AuthProvider provider;
        try {
            provider = User.AuthProvider.valueOf(authRequest.getProvider().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid authentication provider: {}", authRequest.getProvider());
            throw new BadCredentialsException("Invalid authentication provider: " + authRequest.getProvider());
        }

        // Mock user data based on provider (in a real implementation, this would come from the OAuth2 provider)
        String email = "e2e-test-" + provider.name().toLowerCase() + "@example.com";
        String name = "E2E Test User (" + provider.name() + ")";
        String pictureUrl = "https://example.com/" + provider.name().toLowerCase() + ".jpg";
        String providerId = "e2e-" + provider.name().toLowerCase() + "-123";

        // Find or create the user
        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name(name)
                            .pictureUrl(pictureUrl)
                            .provider(provider)
                            .providerId(providerId)
                            .build();
                    return userRepository.save(newUser);
                });

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getEmail());
        long expirationInMs = 86400000; // 24 hours

        return AuthResponse.success(token, UserDTO.fromEntity(user), expirationInMs);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsers(String query) {
        // In a real implementation, this would search by name or email
        // For simplicity, we'll just return all users
        return userRepository.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateProfile(User user, String name, String pictureUrl) {
        user.setName(name);
        user.setPictureUrl(pictureUrl);
        User updatedUser = userRepository.save(user);
        return UserDTO.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public void deleteAccount(User user) {
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByProviderAndProviderId(User.AuthProvider provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }
}
