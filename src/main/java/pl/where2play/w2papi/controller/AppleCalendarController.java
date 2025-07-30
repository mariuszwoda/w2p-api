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
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.service.AppleCalendarService;
import pl.where2play.w2papi.service.UserService;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for Apple Calendar integration endpoints.
 */
@RestController
@RequestMapping("/api/apple-calendar")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Apple Calendar", description = "Apple Calendar integration API")
@SecurityRequirement(name = "Bearer Authentication")
public class AppleCalendarController {

    private final AppleCalendarService appleCalendarService;
    private final UserService userService;

    /**
     * Get the authorization URL for Apple Calendar.
     *
     * @return the authorization URL
     */
    @GetMapping("/auth-url")
    @Operation(summary = "Get authorization URL", description = "Get the OAuth2 authorization URL for Apple Calendar")
    public ResponseEntity<Map<String, String>> getAuthorizationUrl() {
        log.info("Getting Apple Calendar authorization URL");
        String authUrl = appleCalendarService.getAuthorizationUrl();
        Map<String, String> response = new HashMap<>();
        response.put("authorizationUrl", authUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * Exchange an authorization code for an access token.
     *
     * @param code the authorization code
     * @param userDetails the authenticated user details
     * @return a success response
     */
    @PostMapping("/exchange-code")
    @Operation(summary = "Exchange code for token", description = "Exchange an authorization code for an access token")
    public ResponseEntity<Map<String, Boolean>> exchangeCodeForToken(
            @RequestParam String code,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Exchanging authorization code for token for user: {}", userDetails.getUsername());
        User user = userService.getCurrentUser(userDetails.getUsername());
        boolean success = appleCalendarService.exchangeCodeForToken(code, user);

        Map<String, Boolean> response = new HashMap<>();
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if the user has authorized Apple Calendar access.
     *
     * @param userDetails the authenticated user details
     * @return the authorization status
     */
    @GetMapping("/auth-status")
    @Operation(summary = "Check authorization status", description = "Check if the user has authorized Apple Calendar access")
    public ResponseEntity<Map<String, Boolean>> checkAuthorizationStatus(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Checking Apple Calendar authorization status for user: {}", userDetails.getUsername());
        User user = userService.getCurrentUser(userDetails.getUsername());
        boolean isAuthorized = appleCalendarService.isAuthorized(user);

        Map<String, Boolean> response = new HashMap<>();
        response.put("authorized", isAuthorized);
        return ResponseEntity.ok(response);
    }

    /**
     * Synchronize events with Apple Calendar.
     *
     * @param userDetails the authenticated user details
     * @return a success response
     */
    @PostMapping("/sync")
    @Operation(summary = "Synchronize events", description = "Synchronize events with Apple Calendar")
    public ResponseEntity<Map<String, Object>> synchronizeEvents(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Synchronizing events with Apple Calendar for user: {}", userDetails.getUsername());
        User user = userService.getCurrentUser(userDetails.getUsername());
        var events = appleCalendarService.synchronizeEvents(user);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("eventsCount", events.size());
        return ResponseEntity.ok(response);
    }
}