package pl.where2play.w2papi.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
     * @param page the page number (0-based)
     * @param size the page size
     * @param sort the sort field
     * @param direction the sort direction
     * @return the list of event DTOs
     */
    @GetMapping(ApiEndpoint.CalendarEvent.GET_ALL_EVENTS)
    @Operation(summary = "Get all events", description = "Get all calendar events for the current user")
    public ResponseEntity<?> getAllEvents(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        log.info("Getting all calendar events for user: {} (page: {}, size: {})", userDetails.getUsername(), page, size);
        User user = userService.getCurrentUser(userDetails.getUsername());

        // If pagination parameters are not provided, return all events
        if (page < 0 || size <= 0) {
            List<CalendarEventDTO> events = eventService.getAllEventsForUser(user);
            return ResponseEntity.ok(events);
        }

        // Create pageable object
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // Get paginated events
        Page<CalendarEventDTO> events = eventService.getAllEventsForUser(user, pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * Get events in a date range.
     *
     * @param start the start date
     * @param end the end date
     * @param userDetails the authenticated user details
     * @param page the page number (0-based)
     * @param size the page size
     * @param sort the sort field
     * @param direction the sort direction
     * @return the list of event DTOs
     */
    @GetMapping(ApiEndpoint.CalendarEvent.GET_EVENTS_IN_RANGE)
    @Operation(summary = "Get events in date range", description = "Get calendar events for the current user in a date range")
    public ResponseEntity<?> getEventsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        log.info("Getting calendar events for user: {} in date range: {} to {} (page: {}, size: {})", 
                userDetails.getUsername(), start, end, page, size);
        User user = userService.getCurrentUser(userDetails.getUsername());

        // If pagination parameters are not provided, return all events
        if (page < 0 || size <= 0) {
            List<CalendarEventDTO> events = eventService.getEventsInDateRange(user, start, end);
            return ResponseEntity.ok(events);
        }

        // Create pageable object
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // Get paginated events
        Page<CalendarEventDTO> events = eventService.getEventsInDateRange(user, start, end, pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * Synchronize events with an external calendar provider.
     *
     * @param provider the calendar provider
     * @param userDetails the authenticated user details
     * @param page the page number (0-based)
     * @param size the page size
     * @param sort the sort field
     * @param direction the sort direction
     * @return the list of synchronized event DTOs
     */
    @PostMapping(ApiEndpoint.CalendarEvent.SYNC_EVENTS)
    @Operation(summary = "Synchronize events", description = "Synchronize events with an external calendar provider")
    public ResponseEntity<?> synchronizeEvents(
            @RequestParam String provider,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        log.info("Synchronizing calendar events for user: {} with provider: {} (page: {}, size: {})", 
                userDetails.getUsername(), provider, page, size);
        User user = userService.getCurrentUser(userDetails.getUsername());

        CalendarEvent.CalendarProvider calendarProvider;
        try {
            calendarProvider = CalendarEvent.CalendarProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        // If pagination parameters are not provided, return all events
        if (page < 0 || size <= 0) {
            List<CalendarEventDTO> events = eventService.synchronizeEvents(user, calendarProvider);
            return ResponseEntity.ok(events);
        }

        // Create pageable object
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // Get paginated events
        Page<CalendarEventDTO> events = eventService.synchronizeEvents(user, calendarProvider, pageable);
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
