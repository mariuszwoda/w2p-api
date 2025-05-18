package pl.where2play.w2papi.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.where2play.w2papi.constants.ApiEndpoint;
import pl.where2play.w2papi.dto.CalendarEventDTO;
import pl.where2play.w2papi.dto.request.CreateCalendarEventRequest;
import pl.where2play.w2papi.dto.request.UpdateCalendarEventRequest;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.service.CalendarEventService;
import pl.where2play.w2papi.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for calendar event endpoints.
 */
@RestController
@RequestMapping(ApiEndpoint.CalendarEvent.BASE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Calendar Events", description = "Calendar event management API")
@SecurityRequirement(name = "Bearer Authentication")
public class CalendarEventController {

    private final CalendarEventService eventService;
    private final UserService userService;

    /**
     * Create a new calendar event.
     *
     * @param request the event creation request
     * @param userDetails the authenticated user details
     * @return the created event DTO
     */
    @PostMapping(ApiEndpoint.CalendarEvent.CREATE_EVENT)
    @Operation(summary = "Create event", description = "Create a new calendar event")
    public ResponseEntity<CalendarEventDTO> createEvent(
            @Valid @RequestBody CreateCalendarEventRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Creating calendar event for user: {}", userDetails.getUsername());
        User user = userService.getCurrentUser(userDetails.getUsername());
        CalendarEventDTO eventDTO = eventService.createEvent(request, user);
        return ResponseEntity.ok(eventDTO);
    }

    /**
     * Update an existing calendar event.
     *
     * @param id the event ID
     * @param request the event update request
     * @param userDetails the authenticated user details
     * @return the updated event DTO
     */
    @PutMapping(ApiEndpoint.CalendarEvent.UPDATE_EVENT)
    @Operation(summary = "Update event", description = "Update an existing calendar event")
    public ResponseEntity<CalendarEventDTO> updateEvent(
            @PathVariable Long id,
            @RequestBody UpdateCalendarEventRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Updating calendar event with ID: {} for user: {}", id, userDetails.getUsername());
        User user = userService.getCurrentUser(userDetails.getUsername());
        CalendarEventDTO eventDTO = eventService.updateEvent(id, request, user);
        return ResponseEntity.ok(eventDTO);
    }

    /**
     * Soft delete a calendar event.
     * This marks the event as deleted without removing it from the database.
     * This endpoint is only for E2E tests.
     *
     * @param id the event ID
     * @param isE2ETest flag indicating if this is an E2E test
     * @param userDetails the authenticated user details
     * @return a success response
     */
    @DeleteMapping(ApiEndpoint.CalendarEvent.DELETE_EVENT)
    @Operation(summary = "Soft delete event", description = "Soft delete a calendar event (mark as deleted) - E2E tests only")
    @Hidden
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean isE2ETest,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Soft deleting calendar event with ID: {} for user: {}", id, userDetails.getUsername());
        User user = userService.getCurrentUser(userDetails.getUsername());

        if (!isE2ETest) {
            log.warn("Attempted soft delete without E2E test flag for event ID: {}", id);
            return ResponseEntity.badRequest().build();
        }

        eventService.deleteEvent(id, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Hard delete a calendar event.
     * This completely removes the event from the database.
     * This endpoint is only for E2E tests and is not available in production.
     *
     * @param id the event ID
     * @param userDetails the authenticated user details
     * @return a success response
     */
    @DeleteMapping(ApiEndpoint.CalendarEvent.HARD_DELETE_EVENT)
    @Operation(summary = "Hard delete event", description = "Hard delete a calendar event (remove from database) - E2E tests only")
    @Profile("!prod")
    @Hidden
    public ResponseEntity<Void> hardDeleteEvent(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean isE2ETest,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Hard deleting calendar event with ID: {} for user: {}", id, userDetails.getUsername());
        User user = userService.getCurrentUser(userDetails.getUsername());
        try {
            eventService.hardDeleteEvent(id, user, isE2ETest);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Attempted hard delete without E2E test flag: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get a calendar event by ID.
     *
     * @param id the event ID
     * @param userDetails the authenticated user details
     * @return the event DTO
     */
    @GetMapping(ApiEndpoint.CalendarEvent.GET_EVENT)
    @Operation(summary = "Get event", description = "Get a calendar event by ID")
    public ResponseEntity<CalendarEventDTO> getEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Getting calendar event with ID: {} for user: {}", id, userDetails.getUsername());
        try {
            User user = userService.getCurrentUser(userDetails.getUsername());
            CalendarEventDTO eventDTO = eventService.getEvent(id, user);
            return ResponseEntity.ok(eventDTO);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Event not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    /**
     * Get all events for the current user.
     *
     * @param userDetails the authenticated user details
     * @return the list of event DTOs
     */
    @GetMapping(ApiEndpoint.CalendarEvent.GET_ALL_EVENTS)
    @Operation(summary = "Get all events", description = "Get all calendar events for the current user")
    public ResponseEntity<List<CalendarEventDTO>> getAllEvents(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Getting all calendar events for user: {}", userDetails.getUsername());
        User user = userService.getCurrentUser(userDetails.getUsername());
        List<CalendarEventDTO> events = eventService.getAllEventsForUser(user);
        return ResponseEntity.ok(events);
    }

    /**
     * Get events in a date range.
     *
     * @param start the start date
     * @param end the end date
     * @param userDetails the authenticated user details
     * @return the list of event DTOs
     */
    @GetMapping(ApiEndpoint.CalendarEvent.GET_EVENTS_IN_RANGE)
    @Operation(summary = "Get events in date range", description = "Get calendar events for the current user in a date range")
    public ResponseEntity<List<CalendarEventDTO>> getEventsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Getting calendar events for user: {} in date range: {} to {}", userDetails.getUsername(), start, end);
        User user = userService.getCurrentUser(userDetails.getUsername());
        List<CalendarEventDTO> events = eventService.getEventsInDateRange(user, start, end);
        return ResponseEntity.ok(events);
    }

    /**
     * Synchronize events with an external calendar provider.
     *
     * @param provider the calendar provider
     * @param userDetails the authenticated user details
     * @return the list of synchronized event DTOs
     */
    @PostMapping(ApiEndpoint.CalendarEvent.SYNC_EVENTS)
    @Operation(summary = "Synchronize events", description = "Synchronize events with an external calendar provider")
    public ResponseEntity<List<CalendarEventDTO>> synchronizeEvents(
            @RequestParam String provider,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Synchronizing calendar events for user: {} with provider: {}", userDetails.getUsername(), provider);
        User user = userService.getCurrentUser(userDetails.getUsername());

        CalendarEvent.CalendarProvider calendarProvider;
        try {
            calendarProvider = CalendarEvent.CalendarProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        List<CalendarEventDTO> events = eventService.synchronizeEvents(user, calendarProvider);
        return ResponseEntity.ok(events);
    }

    /**
     * Add an attendee to an event.
     *
     * @param eventId the event ID
     * @param userId the user ID to add as attendee
     * @param userDetails the authenticated user details
     * @return the updated event DTO
     */
    @PostMapping(ApiEndpoint.CalendarEvent.ADD_ATTENDEE)
    @Operation(summary = "Add attendee", description = "Add a user as an attendee to an event")
    public ResponseEntity<CalendarEventDTO> addAttendee(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Adding attendee with ID: {} to event with ID: {}", userId, eventId);
        User user = userService.getCurrentUser(userDetails.getUsername());
        CalendarEventDTO eventDTO = eventService.addAttendee(eventId, userId, user);
        return ResponseEntity.ok(eventDTO);
    }

    /**
     * Remove an attendee from an event.
     *
     * @param eventId the event ID
     * @param userId the user ID to remove as attendee
     * @param userDetails the authenticated user details
     * @return the updated event DTO
     */
    @DeleteMapping(ApiEndpoint.CalendarEvent.REMOVE_ATTENDEE)
    @Operation(summary = "Remove attendee", description = "Remove a user as an attendee from an event")
    public ResponseEntity<CalendarEventDTO> removeAttendee(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Removing attendee with ID: {} from event with ID: {}", userId, eventId);
        User user = userService.getCurrentUser(userDetails.getUsername());
        CalendarEventDTO eventDTO = eventService.removeAttendee(eventId, userId, user);
        return ResponseEntity.ok(eventDTO);
    }
}
