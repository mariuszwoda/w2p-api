package pl.where2play.w2papi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for MFA verification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaVerificationRequest {
    
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Code is required")
    private String code;
    
    @NotBlank(message = "Token is required")
    private String token;
}