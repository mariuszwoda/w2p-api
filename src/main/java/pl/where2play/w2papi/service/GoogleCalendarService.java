package pl.where2play.w2papi.service;

import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;

import java.util.List;

/**
 * Service interface for Google Calendar integration.
 */
public interface GoogleCalendarService {

    /**
     * Fetch events from Google Calendar for a user.
     *
     * @param user the user
     * @return the list of calendar events
     */
    List<CalendarEvent> fetchEvents(User user);

    /**
     * Create an event in Google Calendar.
     *
     * @param event the event to create
     * @param user the user
     * @return the created event with external ID
     */
    CalendarEvent createEvent(CalendarEvent event, User user);

    /**
     * Update an event in Google Calendar.
     *
     * @param event the event to update
     * @param user the user
     * @return the updated event
     */
    CalendarEvent updateEvent(CalendarEvent event, User user);

    /**
     * Delete an event from Google Calendar.
     *
     * @param event the event to delete
     * @param user the user
     */
    void deleteEvent(CalendarEvent event, User user);

    /**
     * Synchronize events between the application and Google Calendar.
     *
     * @param user the user
     * @return the list of synchronized events
     */
    List<CalendarEvent> synchronizeEvents(User user);

    /**
     * Get the OAuth2 authorization URL for Google Calendar.
     *
     * @return the authorization URL
     */
    String getAuthorizationUrl();

    /**
     * Exchange an authorization code for an access token.
     *
     * @param code the authorization code
     * @param user the user
     * @return true if the exchange was successful, false otherwise
     */
    boolean exchangeCodeForToken(String code, User user);

    /**
     * Check if a user has authorized Google Calendar access.
     *
     * @param user the user
     * @return true if the user has authorized access, false otherwise
     */
    boolean isAuthorized(User user);
}