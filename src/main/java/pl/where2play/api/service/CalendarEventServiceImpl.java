package pl.where2play.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.where2play.api.exception.ResourceNotFoundException;
import pl.where2play.api.model.CalendarEvent;
import pl.where2play.api.repository.CalendarEventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CalendarEventServiceImpl implements CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;

    @Override
    public List<CalendarEvent> getAllEvents() {
        return calendarEventRepository.findAll();
    }

    @Override
    public Optional<CalendarEvent> getEventById(Long id) {
        return calendarEventRepository.findById(id);
    }

    @Override
    public CalendarEvent createEvent(CalendarEvent event) {
        return calendarEventRepository.save(event);
    }

    @Override
    public CalendarEvent updateEvent(Long id, CalendarEvent eventDetails) {
        return calendarEventRepository.findById(id)
                .map(existingEvent -> {
                    existingEvent.setTitle(eventDetails.getTitle());
                    existingEvent.setDescription(eventDetails.getDescription());
                    existingEvent.setStartTime(eventDetails.getStartTime());
                    existingEvent.setEndTime(eventDetails.getEndTime());
                    existingEvent.setLocation(eventDetails.getLocation());
                    existingEvent.setStatus(eventDetails.getStatus());
                    return calendarEventRepository.save(existingEvent);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    @Override
    public void deleteEvent(Long id) {
        if (!calendarEventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event", id);
        }
        calendarEventRepository.deleteById(id);
    }

    @Override
    public List<CalendarEvent> searchEventsByTitle(String title) {
        return calendarEventRepository.findByTitleContainingIgnoreCase(title);
    }

    @Override
    public List<CalendarEvent> getEventsBetweenDates(LocalDateTime start, LocalDateTime end) {
        return calendarEventRepository.findByStartTimeBetween(start, end);
    }

    @Override
    public List<CalendarEvent> getEventsByStatus(CalendarEvent.EventStatus status) {
        return calendarEventRepository.findByStatus(status);
    }

    @Override
    public List<CalendarEvent> getEventsByLocation(String location) {
        return calendarEventRepository.findByLocationContainingIgnoreCase(location);
    }

    @Override
    public List<CalendarEvent> getEventsByCreator(String createdBy) {
        return calendarEventRepository.findByCreatedBy(createdBy);
    }
}
