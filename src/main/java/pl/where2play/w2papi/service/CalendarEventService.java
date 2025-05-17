package pl.where2play.w2papi.service;

import pl.where2play.w2papi.dto.CalendarEventDTO;
import pl.where2play.w2papi.dto.request.CreateCalendarEventRequest;
import pl.where2play.w2papi.dto.request.UpdateCalendarEventRequest;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing calendar events.
 */
public interface CalendarEventService {

    /**
     * Create a new calendar event.
     *
     * @param request the event creation request
     * @param owner the event owner
     * @return the created event DTO
     */
    CalendarEventDTO createEvent(CreateCalendarEventRequest request, User owner);

    /**
     * Update an existing calendar event.
     *
     * @param eventId the event ID
     * @param request the event update request
     * @param user the user performing the update
     * @return the updated event DTO
     */
    CalendarEventDTO updateEvent(Long eventId, UpdateCalendarEventRequest request, User user);

    /**
     * Soft delete a calendar event.
     * This marks the event as deleted without removing it from the database.
     *
     * @param eventId the event ID
     * @param user the user performing the deletion
     */
    void deleteEvent(Long eventId, User user);

    /**
     * Hard delete a calendar event.
     * This completely removes the event from the database.
     * This method should only be used for E2E tests.
     *
     * @param eventId the event ID
     * @param user the user performing the deletion
     * @param isE2ETest flag indicating if this is an E2E test
     * @throws IllegalArgumentException if isE2ETest is false
     */
    void hardDeleteEvent(Long eventId, User user, boolean isE2ETest);

    /**
     * Get a calendar event by ID.
     *
     * @param eventId the event ID
     * @param user the user requesting the event
     * @return the event DTO
     */
    CalendarEventDTO getEvent(Long eventId, User user);

    /**
     * Get all events for a user.
     *
     * @param user the user
     * @return the list of event DTOs
     */
    List<CalendarEventDTO> getAllEventsForUser(User user);

    /**
     * Get all events for a user in a date range.
     *
     * @param user the user
     * @param start the start date
     * @param end the end date
     * @return the list of event DTOs
     */
    List<CalendarEventDTO> getEventsInDateRange(User user, LocalDateTime start, LocalDateTime end);

    /**
     * Synchronize events with an external calendar provider.
     *
     * @param user the user
     * @param provider the calendar provider
     * @return the list of synchronized event DTOs
     */
    List<CalendarEventDTO> synchronizeEvents(User user, CalendarEvent.CalendarProvider provider);

    /**
     * Add a user as an attendee to an event.
     *
     * @param eventId the event ID
     * @param userId the user ID to add as attendee
     * @param currentUser the user performing the operation
     * @return the updated event DTO
     */
    CalendarEventDTO addAttendee(Long eventId, Long userId, User currentUser);

    /**
     * Remove a user as an attendee from an event.
     *
     * @param eventId the event ID
     * @param userId the user ID to remove as attendee
     * @param currentUser the user performing the operation
     * @return the updated event DTO
     */
    CalendarEventDTO removeAttendee(Long eventId, Long userId, User currentUser);
}
