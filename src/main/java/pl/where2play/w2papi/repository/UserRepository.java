package pl.where2play.w2papi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.where2play.w2papi.model.User;

import java.util.Optional;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find a user by email.
     *
     * @param email the email
     * @return the user
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if a user exists by email.
     *
     * @param email the email
     * @return true if the user exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find a user by provider and providerId.
     *
     * @param provider the provider
     * @param providerId the providerId
     * @return the user
     */
    Optional<User> findByProviderAndProviderId(User.AuthProvider provider, String providerId);
}