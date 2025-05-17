package pl.where2play.w2papi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.where2play.w2papi.dto.CalendarEventDTO;
import pl.where2play.w2papi.dto.request.CreateCalendarEventRequest;
import pl.where2play.w2papi.dto.request.UpdateCalendarEventRequest;
import pl.where2play.w2papi.exception.ForbiddenException;
import pl.where2play.w2papi.exception.ResourceNotFoundException;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.CalendarEventRepository;
import pl.where2play.w2papi.repository.UserRepository;
import pl.where2play.w2papi.service.CalendarEventService;
import pl.where2play.w2papi.service.GoogleCalendarService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the CalendarEventService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarEventServiceImpl implements CalendarEventService {

    private final CalendarEventRepository eventRepository;
    private final UserRepository userRepository;
    private final GoogleCalendarService googleCalendarService;

    @Override
    @Transactional
    public CalendarEventDTO createEvent(CreateCalendarEventRequest request, User owner) {
        log.info("Creating calendar event for user: {}", owner.getEmail());

        // Create the event entity
        CalendarEvent event = CalendarEvent.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .owner(owner)
                .allDay(request.isAllDay())
                .recurrenceRule(request.getRecurrenceRule())
                .build();

        // Set calendar provider if specified
        if (request.getCalendarProvider() != null && !request.getCalendarProvider().isEmpty()) {
            try {
                CalendarEvent.CalendarProvider provider = 
                        CalendarEvent.CalendarProvider.valueOf(request.getCalendarProvider().toUpperCase());
                event.setProvider(provider);

                // If Google Calendar, create the event in Google Calendar
                if (provider == CalendarEvent.CalendarProvider.GOOGLE) {
                    event = googleCalendarService.createEvent(event, owner);
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid calendar provider: {}", request.getCalendarProvider());
            }
        }

        // Add attendees if specified
        if (request.getAttendeeIds() != null && !request.getAttendeeIds().isEmpty()) {
            Set<User> attendees = new HashSet<>();
            for (Long attendeeId : request.getAttendeeIds()) {
                userRepository.findById(attendeeId).ifPresent(attendees::add);
            }
            event.setAttendees(attendees);
        }

        // Save the event
        CalendarEvent savedEvent = eventRepository.save(event);

        return CalendarEventDTO.fromEntity(savedEvent);
    }

    @Override
    @Transactional
    public CalendarEventDTO updateEvent(Long eventId, UpdateCalendarEventRequest request, User user) {
        log.info("Updating calendar event with ID: {} for user: {}", eventId, user.getEmail());

        // Find the event (only non-deleted events)
        CalendarEvent event = eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Event", "ID", eventId));

        // Check if the user is the owner
        if (!event.getOwner().getId().equals(user.getId())) {
            throw ForbiddenException.forResource("Event", eventId);
        }

        // Update the event fields if provided
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }

        if (request.getStartTime() != null) {
            event.setStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null) {
            event.setEndTime(request.getEndTime());
        }

        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }

        if (request.getAllDay() != null) {
            event.setAllDay(request.getAllDay());
        }

        if (request.getRecurrenceRule() != null) {
            event.setRecurrenceRule(request.getRecurrenceRule());
        }

        // Update calendar provider if specified
        if (request.getCalendarProvider() != null && !request.getCalendarProvider().isEmpty()) {
            try {
                CalendarEvent.CalendarProvider provider = 
                        CalendarEvent.CalendarProvider.valueOf(request.getCalendarProvider().toUpperCase());
                event.setProvider(provider);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid calendar provider: {}", request.getCalendarProvider());
            }
        }

        // Update attendees if specified
        if (request.getAttendeeIds() != null) {
            Set<User> attendees = new HashSet<>();
            for (Long attendeeId : request.getAttendeeIds()) {
                userRepository.findById(attendeeId).ifPresent(attendees::add);
            }
            event.setAttendees(attendees);
        }

        // Update in Google Calendar if needed
        if (event.getProvider() == CalendarEvent.CalendarProvider.GOOGLE && event.getExternalId() != null) {
            event = googleCalendarService.updateEvent(event, user);
        }

        // Save the updated event
        CalendarEvent updatedEvent = eventRepository.save(event);

        return CalendarEventDTO.fromEntity(updatedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId, User user) {
        log.info("Soft deleting calendar event with ID: {} for user: {}", eventId, user.getEmail());

        // Find the event (only non-deleted events)
        CalendarEvent event = eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Event", "ID", eventId));

        // Check if the user is the owner
        if (!event.getOwner().getId().equals(user.getId())) {
            throw ForbiddenException.forResource("Event", eventId);
        }

        // Delete from Google Calendar if needed
        if (event.getProvider() == CalendarEvent.CalendarProvider.GOOGLE && event.getExternalId() != null) {
            googleCalendarService.deleteEvent(event, user);
        }

        // Soft delete the event
        event.setDeleted(true);
        event.setDeletedAt(LocalDateTime.now());
        eventRepository.save(event);
    }

    @Override
    @Transactional
    public void hardDeleteEvent(Long eventId, User user, boolean isE2ETest) {
        log.info("Hard deleting calendar event with ID: {} for user: {}", eventId, user.getEmail());

        if (!isE2ETest) {
            throw new IllegalArgumentException("Hard delete is only allowed for E2E tests");
        }

        // Find the event
        CalendarEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Event", "ID", eventId));

        // Check if the user is the owner
        if (!event.getOwner().getId().equals(user.getId())) {
            throw ForbiddenException.forResource("Event", eventId);
        }

        // Delete from Google Calendar if needed
        if (event.getProvider() == CalendarEvent.CalendarProvider.GOOGLE && event.getExternalId() != null) {
            googleCalendarService.deleteEvent(event, user);
        }

        // Hard delete the event
        eventRepository.delete(event);
    }

    @Override
    @Transactional(readOnly = true)
    public CalendarEventDTO getEvent(Long eventId, User user) {
        log.info("Getting calendar event with ID: {} for user: {}", eventId, user.getEmail());

        // Find the event (only non-deleted events)
        CalendarEvent event = eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Event", "ID", eventId));

        // Check if the user is the owner or an attendee
        if (!event.getOwner().getId().equals(user.getId()) && 
                !event.getAttendees().stream().anyMatch(a -> a.getId().equals(user.getId()))) {
            throw ForbiddenException.forResource("Event", eventId);
        }

        return CalendarEventDTO.fromEntity(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventDTO> getAllEventsForUser(User user) {
        log.info("Getting all calendar events for user: {}", user.getEmail());

        List<CalendarEvent> events = eventRepository.findAllEventsForUser(user);

        return events.stream()
                .map(CalendarEventDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventDTO> getEventsInDateRange(User user, LocalDateTime start, LocalDateTime end) {
        log.info("Getting calendar events for user: {} in date range: {} to {}", user.getEmail(), start, end);

        List<CalendarEvent> events = eventRepository.findAllEventsForUserInDateRange(user, start, end);

        return events.stream()
                .map(CalendarEventDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<CalendarEventDTO> synchronizeEvents(User user, CalendarEvent.CalendarProvider provider) {
        log.info("Synchronizing calendar events for user: {} with provider: {}", user.getEmail(), provider);

        List<CalendarEvent> events;

        if (provider == CalendarEvent.CalendarProvider.GOOGLE) {
            events = googleCalendarService.synchronizeEvents(user);
        } else {
            throw new UnsupportedOperationException("Calendar provider not supported: " + provider);
        }

        return events.stream()
                .map(CalendarEventDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CalendarEventDTO addAttendee(Long eventId, Long userId, User currentUser) {
        log.info("Adding attendee with ID: {} to event with ID: {}", userId, eventId);

        // Find the event (only non-deleted events)
        CalendarEvent event = eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Event", "ID", eventId));

        // Check if the current user is the owner
        if (!event.getOwner().getId().equals(currentUser.getId())) {
            throw ForbiddenException.forResource("Event", eventId);
        }

        // Find the user to add as attendee
        User attendee = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.forResource("User", "ID", userId));

        // Add the attendee
        event.getAttendees().add(attendee);

        // Update in Google Calendar if needed
        if (event.getProvider() == CalendarEvent.CalendarProvider.GOOGLE && event.getExternalId() != null) {
            event = googleCalendarService.updateEvent(event, currentUser);
        }

        // Save the updated event
        CalendarEvent updatedEvent = eventRepository.save(event);

        return CalendarEventDTO.fromEntity(updatedEvent);
    }

    @Override
    @Transactional
    public CalendarEventDTO removeAttendee(Long eventId, Long userId, User currentUser) {
        log.info("Removing attendee with ID: {} from event with ID: {}", userId, eventId);

        // Find the event (only non-deleted events)
        CalendarEvent event = eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Event", "ID", eventId));

        // Check if the current user is the owner
        if (!event.getOwner().getId().equals(currentUser.getId())) {
            throw ForbiddenException.forResource("Event", eventId);
        }

        // Find the user to remove as attendee
        User attendee = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.forResource("User", "ID", userId));

        // Remove the attendee
        event.getAttendees().remove(attendee);

        // Update in Google Calendar if needed
        if (event.getProvider() == CalendarEvent.CalendarProvider.GOOGLE && event.getExternalId() != null) {
            event = googleCalendarService.updateEvent(event, currentUser);
        }

        // Save the updated event
        CalendarEvent updatedEvent = eventRepository.save(event);

        return CalendarEventDTO.fromEntity(updatedEvent);
    }
}
