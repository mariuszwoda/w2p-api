package pl.where2play.w2papi.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.CalendarEventRepository;
import pl.where2play.w2papi.service.GoogleCalendarService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the GoogleCalendarService interface.
 * This is a simplified implementation for demonstration purposes.
 * In a real application, you would need to handle OAuth2 flow properly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarServiceImpl implements GoogleCalendarService {

    private final CalendarEventRepository eventRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private static final String APPLICATION_NAME = "W2P Calendar API";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Override
    @Transactional
    public List<CalendarEvent> fetchEvents(User user) {
        log.info("Fetching events from Google Calendar for user: {}", user.getEmail());

        try {
            // Get Google Calendar service
            Calendar service = getCalendarService(user);

            // List events
            com.google.api.services.calendar.model.Events events = service.events().list("primary")
                    .setMaxResults(100)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            // Convert Google Calendar events to our model
            List<CalendarEvent> calendarEvents = new ArrayList<>();
            for (Event event : events.getItems()) {
                CalendarEvent calendarEvent = convertToCalendarEvent(event, user);
                calendarEvents.add(calendarEvent);
            }

            return calendarEvents;
        } catch (Exception e) {
            log.error("Error fetching events from Google Calendar", e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public CalendarEvent createEvent(CalendarEvent event, User user) {
        log.info("Creating event in Google Calendar for user: {}", user.getEmail());

        try {
            // Get Google Calendar service
            Calendar service = getCalendarService(user);

            // Convert our model to Google Calendar event
            Event googleEvent = convertToGoogleEvent(event);

            // Create event
            Event createdEvent = service.events().insert("primary", googleEvent).execute();

            // Update our model with external ID
            event.setExternalId(createdEvent.getId());
            event.setProvider(CalendarEvent.CalendarProvider.GOOGLE);

            return event;
        } catch (Exception e) {
            log.error("Error creating event in Google Calendar", e);
            return event;
        }
    }

    @Override
    @Transactional
    public CalendarEvent updateEvent(CalendarEvent event, User user) {
        log.info("Updating event in Google Calendar for user: {}", user.getEmail());

        try {
            // Get Google Calendar service
            Calendar service = getCalendarService(user);

            // Convert our model to Google Calendar event
            Event googleEvent = convertToGoogleEvent(event);

            // Update event
            service.events().update("primary", event.getExternalId(), googleEvent).execute();

            return event;
        } catch (Exception e) {
            log.error("Error updating event in Google Calendar", e);
            return event;
        }
    }

    @Override
    @Transactional
    public void deleteEvent(CalendarEvent event, User user) {
        log.info("Deleting event from Google Calendar for user: {}", user.getEmail());

        try {
            // Get Google Calendar service
            Calendar service = getCalendarService(user);

            // Delete event
            service.events().delete("primary", event.getExternalId()).execute();
        } catch (Exception e) {
            log.error("Error deleting event from Google Calendar", e);
        }
    }

    @Override
    @Transactional
    public List<CalendarEvent> synchronizeEvents(User user) {
        log.info("Synchronizing events with Google Calendar for user: {}", user.getEmail());

        // Fetch events from Google Calendar
        List<CalendarEvent> googleEvents = fetchEvents(user);

        // Get existing events from our database
        List<CalendarEvent> existingEvents = eventRepository.findByOwner(user).stream()
                .filter(e -> e.getProvider() == CalendarEvent.CalendarProvider.GOOGLE && e.getExternalId() != null)
                .collect(Collectors.toList());

        // Create a map of external IDs to existing events
        java.util.Map<String, CalendarEvent> existingEventMap = existingEvents.stream()
                .collect(Collectors.toMap(CalendarEvent::getExternalId, e -> e));

        // Process Google events
        List<CalendarEvent> result = new ArrayList<>();
        for (CalendarEvent googleEvent : googleEvents) {
            CalendarEvent existingEvent = existingEventMap.get(googleEvent.getExternalId());

            if (existingEvent != null) {
                // Update existing event
                existingEvent.setTitle(googleEvent.getTitle());
                existingEvent.setDescription(googleEvent.getDescription());
                existingEvent.setStartTime(googleEvent.getStartTime());
                existingEvent.setEndTime(googleEvent.getEndTime());
                existingEvent.setLocation(googleEvent.getLocation());
                existingEvent.setAllDay(googleEvent.isAllDay());
                existingEvent.setRecurrenceRule(googleEvent.getRecurrenceRule());

                result.add(eventRepository.save(existingEvent));

                // Remove from map to track processed events
                existingEventMap.remove(googleEvent.getExternalId());
            } else {
                // Create new event
                googleEvent.setOwner(user);
                result.add(eventRepository.save(googleEvent));
            }
        }

        // Delete events that no longer exist in Google Calendar
        for (CalendarEvent orphanedEvent : existingEventMap.values()) {
            eventRepository.delete(orphanedEvent);
        }

        return result;
    }

    @Override
    public String getAuthorizationUrl() {
        // In a real implementation, this would generate an OAuth2 authorization URL
        return "https://accounts.google.com/o/oauth2/auth?client_id=" + clientId + "&redirect_uri=http://localhost:8080/oauth2/callback/google&scope=https://www.googleapis.com/auth/calendar&response_type=code";
    }

    @Override
    public boolean exchangeCodeForToken(String code, User user) {
        // In a real implementation, this would exchange the authorization code for an access token
        // and store the token for the user
        log.info("Exchanging authorization code for token for user: {}", user.getEmail());
        return true;
    }

    @Override
    public boolean isAuthorized(User user) {
        // In a real implementation, this would check if the user has a valid access token
        log.info("Checking if user is authorized for Google Calendar: {}", user.getEmail());
        return true;
    }

    /**
     * Get a Google Calendar service instance for a user.
     * In a real implementation, this would use the user's stored access token.
     */
    private Calendar getCalendarService(User user) throws GeneralSecurityException, IOException {
        // In a real implementation, this would use the user's stored access token
        // For demonstration purposes, we'll create a mock service

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Create a mock credential (in a real app, this would use the user's token)
        GoogleCredential credential = new GoogleCredential.Builder()
                .setClientSecrets(clientId, clientSecret)
                .setJsonFactory(JSON_FACTORY)
                .setTransport(HTTP_TRANSPORT)
                .build();

        // Build the calendar service
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Convert a Google Calendar event to our model.
     */
    private CalendarEvent convertToCalendarEvent(Event googleEvent, User owner) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(googleEvent.getSummary());
        event.setDescription(googleEvent.getDescription());
        event.setLocation(googleEvent.getLocation());
        event.setExternalId(googleEvent.getId());
        event.setProvider(CalendarEvent.CalendarProvider.GOOGLE);
        event.setOwner(owner);

        // Set start and end times
        if (googleEvent.getStart() != null) {
            if (googleEvent.getStart().getDateTime() != null) {
                event.setStartTime(java.time.LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(googleEvent.getStart().getDateTime().getValue()), 
                        ZoneId.systemDefault()));
                event.setAllDay(false);
            } else if (googleEvent.getStart().getDate() != null) {
                event.setStartTime(java.time.LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(googleEvent.getStart().getDate().getValue()), 
                        ZoneId.systemDefault()));
                event.setAllDay(true);
            }
        }

        if (googleEvent.getEnd() != null) {
            if (googleEvent.getEnd().getDateTime() != null) {
                event.setEndTime(java.time.LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(googleEvent.getEnd().getDateTime().getValue()), 
                        ZoneId.systemDefault()));
            } else if (googleEvent.getEnd().getDate() != null) {
                event.setEndTime(java.time.LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(googleEvent.getEnd().getDate().getValue()), 
                        ZoneId.systemDefault()));
            }
        }

        // Set recurrence rule
        if (googleEvent.getRecurrence() != null && !googleEvent.getRecurrence().isEmpty()) {
            event.setRecurrenceRule(googleEvent.getRecurrence().get(0));
        }

        return event;
    }

    /**
     * Convert our model to a Google Calendar event.
     */
    private Event convertToGoogleEvent(CalendarEvent calendarEvent) {
        Event event = new Event();
        event.setSummary(calendarEvent.getTitle());
        event.setDescription(calendarEvent.getDescription());
        event.setLocation(calendarEvent.getLocation());

        // Set start and end times
        EventDateTime start = new EventDateTime();
        EventDateTime end = new EventDateTime();

        if (calendarEvent.isAllDay()) {
            // All-day event
            start.setDate(new DateTime(calendarEvent.getStartTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()));
            end.setDate(new DateTime(calendarEvent.getEndTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()));
        } else {
            // Timed event
            start.setDateTime(new DateTime(calendarEvent.getStartTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()));
            end.setDateTime(new DateTime(calendarEvent.getEndTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()));
        }

        event.setStart(start);
        event.setEnd(end);

        // Set recurrence rule
        if (calendarEvent.getRecurrenceRule() != null && !calendarEvent.getRecurrenceRule().isEmpty()) {
            event.setRecurrence(Collections.singletonList(calendarEvent.getRecurrenceRule()));
        }

        return event;
    }
}
