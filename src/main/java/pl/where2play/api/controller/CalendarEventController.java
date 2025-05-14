package pl.where2play.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.where2play.api.config.E2ETestOnly;
import pl.where2play.api.config.E2ETestSupport;
import pl.where2play.api.exception.ResourceNotFoundException;
import pl.where2play.api.model.CalendarEvent;
import pl.where2play.api.service.CalendarEventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class CalendarEventController {

    private final CalendarEventService calendarEventService;

    @GetMapping
    public ResponseEntity<List<CalendarEvent>> getAllEvents() {
        return ResponseEntity.ok(calendarEventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalendarEvent> getEventById(@PathVariable Long id) {
        return calendarEventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", id));
    }

    @PostMapping
    public ResponseEntity<CalendarEvent> createEvent(@Valid @RequestBody CalendarEvent event) {
        CalendarEvent createdEvent = calendarEventService.createEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CalendarEvent> updateEvent(@PathVariable Long id, @Valid @RequestBody CalendarEvent event) {
        CalendarEvent updatedEvent = calendarEventService.updateEvent(id, event);
        return ResponseEntity.ok(updatedEvent);
    }

    @E2ETestSupport
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventForTesting(@PathVariable Long id) {
        calendarEventService.deleteEventForTesting(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<CalendarEvent>> searchEventsByTitle(@RequestParam String title) {
        return ResponseEntity.ok(calendarEventService.searchEventsByTitle(title));
    }

    @GetMapping("/between")
    public ResponseEntity<List<CalendarEvent>> getEventsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(calendarEventService.getEventsBetweenDates(start, end));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CalendarEvent>> getEventsByStatus(@PathVariable CalendarEvent.EventStatus status) {
        return ResponseEntity.ok(calendarEventService.getEventsByStatus(status));
    }

    @GetMapping("/location")
    public ResponseEntity<List<CalendarEvent>> getEventsByLocation(@RequestParam String location) {
        return ResponseEntity.ok(calendarEventService.getEventsByLocation(location));
    }

    @GetMapping("/creator")
    public ResponseEntity<List<CalendarEvent>> getEventsByCreator(@RequestParam String createdBy) {
        return ResponseEntity.ok(calendarEventService.getEventsByCreator(createdBy));
    }
}
