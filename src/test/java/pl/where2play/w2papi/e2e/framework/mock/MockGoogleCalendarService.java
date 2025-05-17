package pl.where2play.w2papi.e2e.framework.mock;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.service.GoogleCalendarService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mock implementation of GoogleCalendarService for testing purposes.
 * This implementation doesn't make real API calls to Google Calendar.
 */
@Service
@Primary
@Profile("test")
public class MockGoogleCalendarService implements GoogleCalendarService {

    @Override
    public List<CalendarEvent> fetchEvents(User user) {
        // Return an empty list for testing
        return new ArrayList<>();
    }

    @Override
    public CalendarEvent createEvent(CalendarEvent event, User user) {
        // Set a mock external ID and return the event
        event.setExternalId("mock-" + UUID.randomUUID());
        event.setProvider(CalendarEvent.CalendarProvider.GOOGLE);
        return event;
    }

    @Override
    public CalendarEvent updateEvent(CalendarEvent event, User user) {
        // Just return the event as is
        return event;
    }

    @Override
    public void deleteEvent(CalendarEvent event, User user) {
        // Do nothing
    }

    @Override
    public List<CalendarEvent> synchronizeEvents(User user) {
        // Return an empty list for testing
        return new ArrayList<>();
    }

    @Override
    public String getAuthorizationUrl() {
        // Return a mock URL that contains accounts.google.com
        return "https://accounts.google.com/o/oauth2/auth?client_id=mock-client-id&redirect_uri=http://localhost:8080/oauth2/callback/google&scope=https://www.googleapis.com/auth/calendar&response_type=code";
    }

    @Override
    public boolean exchangeCodeForToken(String code, User user) {
        // Always return true for testing
        return true;
    }

    @Override
    public boolean isAuthorized(User user) {
        // Always return true for testing
        return true;
    }
}
