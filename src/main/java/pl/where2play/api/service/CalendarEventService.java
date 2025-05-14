package pl.where2play.api.service;

import pl.where2play.api.config.E2ETestOnly;
import pl.where2play.api.model.CalendarEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CalendarEventService {
    
    // CRUD operations
    List<CalendarEvent> getAllEvents();
    
    Optional<CalendarEvent> getEventById(Long id);
    
    CalendarEvent createEvent(CalendarEvent event);
    
    CalendarEvent updateEvent(Long id, CalendarEvent event);

    /**
     * Deletes a user for E2E testing purposes.
     * <p>
     * <strong>WARNING: THIS METHOD IS FOR E2E TEST FRAMEWORK USE ONLY!</strong>
     * <p>
     * This method should only be called from designated E2E test support endpoints
     * and is only available in local, dev, and sit environments.
     *
     * @param id The ID of the user to delete
     * @throws UnsupportedOperationException if called from inappropriate context
     */
    @E2ETestOnly
    void deleteEventForTesting(Long id);
    
    // Additional business operations
    List<CalendarEvent> searchEventsByTitle(String title);
    
    List<CalendarEvent> getEventsBetweenDates(LocalDateTime start, LocalDateTime end);
    
    List<CalendarEvent> getEventsByStatus(CalendarEvent.EventStatus status);
    
    List<CalendarEvent> getEventsByLocation(String location);
    
    List<CalendarEvent> getEventsByCreator(String createdBy);
}