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
    private boolean mfaRequired;
    private String mfaToken;

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
                .mfaRequired(false)
                .build();
    }

    /**
     * Creates an authentication response that requires MFA verification.
     *
     * @param mfaToken temporary token for MFA verification
     * @param user user requiring MFA
     * @param expiresIn token expiration time in milliseconds
     * @return authentication response with MFA required
     */
    public static AuthResponse mfaRequired(String mfaToken, UserDTO user, long expiresIn) {
        return AuthResponse.builder()
                .mfaToken(mfaToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(user)
                .mfaRequired(true)
                .build();
    }
}
