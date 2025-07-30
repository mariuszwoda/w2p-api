package pl.where2play.w2papi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.where2play.w2papi.constants.ApiEndpoint;
import pl.where2play.w2papi.dto.request.AuthRequest;
import pl.where2play.w2papi.dto.request.MfaVerificationRequest;
import pl.where2play.w2papi.dto.response.AuthResponse;
import pl.where2play.w2papi.service.UserService;

/**
 * Controller for authentication endpoints.
 */
@RestController
@RequestMapping(ApiEndpoint.Auth.BASE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {

    private final UserService userService;

    /**
     * Authenticate a user with an OAuth2 token.
     *
     * @param authRequest the authentication request
     * @return the authentication response with JWT token or MFA token if MFA is required
     */
    @PostMapping(ApiEndpoint.Auth.LOGIN)
    @Operation(summary = "Authenticate user", description = "Authenticate a user with an OAuth2 token from Google, Facebook, Twitter, GitHub, Microsoft, or Apple")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        log.info("Authentication request received for provider: {}", authRequest.getProvider());
        AuthResponse response = userService.authenticate(authRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify MFA code and complete authentication.
     *
     * @param mfaRequest the MFA verification request
     * @return the authentication response with JWT token
     */
    @PostMapping(ApiEndpoint.Auth.VERIFY_MFA)
    @Operation(summary = "Verify MFA code", description = "Verify MFA code and complete authentication")
    public ResponseEntity<AuthResponse> verifyMfa(@Valid @RequestBody MfaVerificationRequest mfaRequest) {
        log.info("MFA verification request received for email: {}", mfaRequest.getEmail());
        AuthResponse response = userService.verifyMfa(mfaRequest);
        return ResponseEntity.ok(response);
    }
}
