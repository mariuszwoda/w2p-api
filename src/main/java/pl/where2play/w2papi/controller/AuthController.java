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
import pl.where2play.w2papi.dto.request.AuthRequest;
import pl.where2play.w2papi.dto.response.AuthResponse;
import pl.where2play.w2papi.service.UserService;

/**
 * Controller for authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {

    private final UserService userService;

    /**
     * Authenticate a user with an OAuth2 token.
     *
     * @param authRequest the authentication request
     * @return the authentication response with JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticate a user with an OAuth2 token from Google or Facebook")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        log.info("Authentication request received for provider: {}", authRequest.getProvider());
        AuthResponse response = userService.authenticate(authRequest);
        return ResponseEntity.ok(response);
    }
}