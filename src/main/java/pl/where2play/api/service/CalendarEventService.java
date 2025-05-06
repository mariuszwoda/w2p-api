package pl.where2play.api.service;

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
    
    void deleteEvent(Long id);
    
    // Additional business operations
    List<CalendarEvent> searchEventsByTitle(String title);
    
    List<CalendarEvent> getEventsBetweenDates(LocalDateTime start, LocalDateTime end);
    
    List<CalendarEvent> getEventsByStatus(CalendarEvent.EventStatus status);
    
    List<CalendarEvent> getEventsByLocation(String location);
    
    List<CalendarEvent> getEventsByCreator(String createdBy);
}