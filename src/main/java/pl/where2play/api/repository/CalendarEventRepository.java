package pl.where2play.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.where2play.api.model.CalendarEvent;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    
    // Find events by title (case-insensitive, partial match)
    List<CalendarEvent> findByTitleContainingIgnoreCase(String title);
    
    // Find events between start and end times
    List<CalendarEvent> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    
    // Find events by status
    List<CalendarEvent> findByStatus(CalendarEvent.EventStatus status);
    
    // Find events by location (case-insensitive, partial match)
    List<CalendarEvent> findByLocationContainingIgnoreCase(String location);
    
    // Find events created by a specific user
    List<CalendarEvent> findByCreatedBy(String createdBy);
}