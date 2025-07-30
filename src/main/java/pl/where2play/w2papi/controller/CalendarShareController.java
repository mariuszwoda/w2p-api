package pl.where2play.w2papi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.where2play.w2papi.constants.ApiEndpoint;
import pl.where2play.w2papi.dto.CalendarEventDTO;
import pl.where2play.w2papi.dto.UserDTO;
import pl.where2play.w2papi.dto.request.ShareEventRequest;
import pl.where2play.w2papi.dto.response.CalendarShareDTO;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.CalendarShare;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.service.CalendarEventService;
import pl.where2play.w2papi.service.CalendarShareService;
import pl.where2play.w2papi.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for calendar share endpoints.
 */
@RestController
@RequestMapping(ApiEndpoint.CalendarShare.BASE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Calendar Shares", description = "Calendar sharing API")
@SecurityRequirement(name = "Bearer Authentication")
public class CalendarShareController {

    private final CalendarShareService shareService;
    private final CalendarEventService eventService;
    private final UserService userService;

    /**
     * Share an event with a user.
     *
     * @param request the share request
     * @param userDetails the authenticated user details
     * @return the created share DTO
     */
    @PostMapping(ApiEndpoint.CalendarShare.SHARE_WITH_USER)
    @Operation(summary = "Share with user", description = "Share a calendar event with a registered user")
    public ResponseEntity<CalendarShareDTO> shareWithUser(
            @Valid @RequestBody ShareEventRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Sharing event with ID: {} with user ID: {}", request.getEventId(), request.getRecipientId());

        if (request.getRecipientId() == null) {
            return ResponseEntity.badRequest().build();
        }

        User currentUser = userService.getCurrentUser(userDetails.getUsername());

        // Get the event
        CalendarEventDTO eventDTO = eventService.getEvent(request.getEventId(), currentUser);

        // Get the recipient user
        UserDTO recipientDTO = userService.getUserById(request.getRecipientId());
        User recipient = userService.getCurrentUser(recipientDTO.getEmail());

        // We need to get the actual CalendarEvent entity
        // Since we don't have a direct method, we'll use the event from the user's events
        CalendarEvent event = currentUser.getEvents().stream()
                .filter(e -> e.getId().equals(request.getEventId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        CalendarShare share = shareService.shareEventWithUser(
                event, 
                recipient, 
                request.getPermissionLevel(), 
                request.getExpiresAt()
        );

        return ResponseEntity.ok(CalendarShareDTO.fromEntity(share));
    }

    /**
     * Share an event with an email address.
     *
     * @param request the share request
     * @param userDetails the authenticated user details
     * @return the created share DTO
     */
    @PostMapping(ApiEndpoint.CalendarShare.SHARE_WITH_EMAIL)
    @Operation(summary = "Share with email", description = "Share a calendar event with an email address")
    public ResponseEntity<CalendarShareDTO> shareWithEmail(
            @Valid @RequestBody ShareEventRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Sharing event with ID: {} with email: {}", request.getEventId(), request.getRecipientEmail());

        if (request.getRecipientEmail() == null) {
            return ResponseEntity.badRequest().build();
        }

        User currentUser = userService.getCurrentUser(userDetails.getUsername());

        // Get the event
        CalendarEventDTO eventDTO = eventService.getEvent(request.getEventId(), currentUser);

        // We need to get the actual CalendarEvent entity
        // Since we don't have a direct method, we'll use the event from the user's events
        CalendarEvent event = currentUser.getEvents().stream()
                .filter(e -> e.getId().equals(request.getEventId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        CalendarShare share = shareService.shareEventWithEmail(
                event, 
                request.getRecipientEmail(), 
                request.getPermissionLevel(), 
                request.getExpiresAt()
        );

        return ResponseEntity.ok(CalendarShareDTO.fromEntity(share));
    }

    /**
     * Get all shares for an event.
     *
     * @param eventId the event ID
     * @param userDetails the authenticated user details
     * @return the list of share DTOs
     */
    @GetMapping(ApiEndpoint.CalendarShare.GET_SHARES_FOR_EVENT)
    @Operation(summary = "Get shares for event", description = "Get all shares for a calendar event")
    public ResponseEntity<List<CalendarShareDTO>> getSharesForEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Getting shares for event with ID: {}", eventId);

        User currentUser = userService.getCurrentUser(userDetails.getUsername());

        // Get the event
        CalendarEventDTO eventDTO = eventService.getEvent(eventId, currentUser);

        // We need to get the actual CalendarEvent entity
        // Since we don't have a direct method, we'll use the event from the user's events
        CalendarEvent event = currentUser.getEvents().stream()
                .filter(e -> e.getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        List<CalendarShare> shares = shareService.getSharesForEvent(event);
        List<CalendarShareDTO> shareDTOs = shares.stream()
                .map(CalendarShareDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(shareDTOs);
    }

    /**
     * Get all events shared with the current user.
     *
     * @param userDetails the authenticated user details
     * @return the list of event DTOs
     */
    @GetMapping(ApiEndpoint.CalendarShare.GET_SHARED_EVENTS)
    @Operation(summary = "Get shared events", description = "Get all events shared with the current user")
    public ResponseEntity<List<CalendarEventDTO>> getSharedEvents(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Getting events shared with user: {}", userDetails.getUsername());

        User currentUser = userService.getCurrentUser(userDetails.getUsername());
        List<CalendarEvent> events = shareService.getEventsSharedWithUser(currentUser);

        // Convert events to DTOs
        List<CalendarEventDTO> eventDTOs = events.stream()
                .map(event -> {
                    try {
                        return eventService.getEvent(event.getId(), currentUser);
                    } catch (Exception e) {
                        log.error("Error getting event with ID: {}", event.getId(), e);
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        return ResponseEntity.ok(eventDTOs);
    }

    /**
     * Get all events shared with an email address.
     *
     * @param email the email address
     * @param userDetails the authenticated user details
     * @return the list of event DTOs
     */
    @GetMapping(ApiEndpoint.CalendarShare.GET_SHARED_EVENTS_BY_EMAIL)
    @Operation(summary = "Get shared events by email", description = "Get all events shared with an email address")
    public ResponseEntity<List<CalendarEventDTO>> getSharedEventsByEmail(
            @RequestParam String email,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Getting events shared with email: {}", email);

        // Verify that the email belongs to the current user
        User currentUser = userService.getCurrentUser(userDetails.getUsername());
        if (!currentUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CalendarEvent> events = shareService.getEventsSharedWithEmail(email);

        // Convert events to DTOs
        List<CalendarEventDTO> eventDTOs = events.stream()
                .map(event -> {
                    try {
                        return eventService.getEvent(event.getId(), currentUser);
                    } catch (Exception e) {
                        log.error("Error getting event with ID: {}", event.getId(), e);
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        return ResponseEntity.ok(eventDTOs);
    }

    /**
     * Update a share's permission level.
     *
     * @param shareId the share ID
     * @param permissionLevel the new permission level
     * @param userDetails the authenticated user details
     * @return the updated share DTO
     */
    @PutMapping(ApiEndpoint.CalendarShare.UPDATE_PERMISSION)
    @Operation(summary = "Update permission", description = "Update a share's permission level")
    public ResponseEntity<CalendarShareDTO> updatePermission(
            @PathVariable Long shareId,
            @RequestParam CalendarShare.PermissionLevel permissionLevel,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Updating permission level for share with ID: {} to: {}", shareId, permissionLevel);

        User currentUser = userService.getCurrentUser(userDetails.getUsername());
        CalendarShare share = shareService.updateSharePermission(shareId, permissionLevel, currentUser);

        return ResponseEntity.ok(CalendarShareDTO.fromEntity(share));
    }

    /**
     * Update a share's expiration date.
     *
     * @param shareId the share ID
     * @param expiresAt the new expiration date
     * @param userDetails the authenticated user details
     * @return the updated share DTO
     */
    @PutMapping(ApiEndpoint.CalendarShare.UPDATE_EXPIRATION)
    @Operation(summary = "Update expiration", description = "Update a share's expiration date")
    public ResponseEntity<CalendarShareDTO> updateExpiration(
            @PathVariable Long shareId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiresAt,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Updating expiration date for share with ID: {} to: {}", shareId, expiresAt);

        User currentUser = userService.getCurrentUser(userDetails.getUsername());
        CalendarShare share = shareService.updateShareExpiration(shareId, expiresAt, currentUser);

        return ResponseEntity.ok(CalendarShareDTO.fromEntity(share));
    }

    /**
     * Delete a share.
     *
     * @param shareId the share ID
     * @param userDetails the authenticated user details
     * @return a success response
     */
    @DeleteMapping(ApiEndpoint.CalendarShare.DELETE_SHARE)
    @Operation(summary = "Delete share", description = "Delete a calendar share")
    public ResponseEntity<Void> deleteShare(
            @PathVariable Long shareId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Deleting share with ID: {}", shareId);

        User currentUser = userService.getCurrentUser(userDetails.getUsername());
        shareService.deleteShare(shareId, currentUser);

        return ResponseEntity.noContent().build();
    }

    /**
     * Get a share by its token.
     *
     * @param token the share token
     * @return the share DTO
     */
    @GetMapping(ApiEndpoint.CalendarShare.GET_BY_TOKEN)
    @Operation(summary = "Get share by token", description = "Get a calendar share by its token")
    public ResponseEntity<CalendarShareDTO> getShareByToken(
            @PathVariable("token") String token) {
        log.info("Getting share by token: {}", token);

        try {
            CalendarShare share = shareService.getShareByToken(token);

            // Check if share has expired
            if (share.getExpiresAt() != null && share.getExpiresAt().isBefore(LocalDateTime.now())) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(CalendarShareDTO.fromEntity(share));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Regenerate a share token.
     *
     * @param shareId the share ID
     * @param userDetails the authenticated user details
     * @return the updated share DTO
     */
    @PostMapping(ApiEndpoint.CalendarShare.REGENERATE_TOKEN)
    @Operation(summary = "Regenerate token", description = "Generate a new token for a calendar share")
    public ResponseEntity<CalendarShareDTO> regenerateToken(
            @PathVariable Long shareId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Regenerating token for share with ID: {}", shareId);

        User currentUser = userService.getCurrentUser(userDetails.getUsername());
        CalendarShare share = shareService.regenerateShareToken(shareId, currentUser);

        return ResponseEntity.ok(CalendarShareDTO.fromEntity(share));
    }
}
