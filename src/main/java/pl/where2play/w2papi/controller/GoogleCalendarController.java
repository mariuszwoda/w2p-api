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
import pl.where2play.w2papi.constants.ApiEndpoint;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.service.GoogleCalendarService;
import pl.where2play.w2papi.service.UserService;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for Google Calendar integration endpoints.
 */
@RestController
@RequestMapping(ApiEndpoint.GoogleCalendar.BASE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Google Calendar", description = "Google Calendar integration API")
@SecurityRequirement(name = "Bearer Authentication")
public class GoogleCalendarController {

    private final GoogleCalendarService googleCalendarService;
    private final UserService userService;

    /**
     * Get the authorization URL for Google Calendar.
     *
     * @return the authorization URL
     */
    @GetMapping(ApiEndpoint.GoogleCalendar.AUTH_URL)
    @Operation(summary = "Get authorization URL", description = "Get the OAuth2 authorization URL for Google Calendar")
    public ResponseEntity<Map<String, String>> getAuthorizationUrl() {
        log.info("Getting Google Calendar authorization URL");
        String authUrl = googleCalendarService.getAuthorizationUrl();
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
    @PostMapping(ApiEndpoint.GoogleCalendar.EXCHANGE_CODE)
    @Operation(summary = "Exchange code for token", description = "Exchange an authorization code for an access token")
    public ResponseEntity<Map<String, Boolean>> exchangeCodeForToken(
            @RequestParam String code,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Exchanging authorization code for token for user: {}", userDetails.getUsername());
        User user = userService.getCurrentUser(userDetails.getUsername());
        boolean success = googleCalendarService.exchangeCodeForToken(code, user);

        Map<String, Boolean> response = new HashMap<>();
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if the user has authorized Google Calendar access.
     *
     * @param userDetails the authenticated user details
     * @return the authorization status
     */
    @GetMapping(ApiEndpoint.GoogleCalendar.AUTH_STATUS)
    @Operation(summary = "Check authorization status", description = "Check if the user has authorized Google Calendar access")
    public ResponseEntity<Map<String, Boolean>> checkAuthorizationStatus(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Checking Google Calendar authorization status for user: {}", userDetails.getUsername());
        User user = userService.getCurrentUser(userDetails.getUsername());
        boolean isAuthorized = googleCalendarService.isAuthorized(user);

        Map<String, Boolean> response = new HashMap<>();
        response.put("authorized", isAuthorized);
        return ResponseEntity.ok(response);
    }
}
