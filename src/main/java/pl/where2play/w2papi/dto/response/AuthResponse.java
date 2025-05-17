package pl.where2play.w2papi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.where2play.w2papi.dto.UserDTO;

/**
 * Response DTO for user authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String tokenType;
    private long expiresIn;
    private UserDTO user;
    
    /**
     * Creates a successful authentication response.
     *
     * @param token JWT token
     * @param user authenticated user
     * @param expiresIn token expiration time in milliseconds
     * @return authentication response
     */
    public static AuthResponse success(String token, UserDTO user, long expiresIn) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(user)
                .build();
    }
}