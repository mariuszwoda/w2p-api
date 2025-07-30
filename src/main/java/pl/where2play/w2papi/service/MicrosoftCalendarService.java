package pl.where2play.w2papi.service;

import pl.where2play.w2papi.dto.CalendarEventDTO;
import pl.where2play.w2papi.model.User;

import java.util.List;

/**
 * Service interface for Microsoft Calendar integration.
 */
public interface MicrosoftCalendarService {

    /**
     * Get the authorization URL for Microsoft Calendar.
     *
     * @return the authorization URL
     */
    String getAuthorizationUrl();

    /**
     * Exchange an authorization code for an access token.
     *
     * @param code the authorization code
     * @param user the user
     * @return true if successful, false otherwise
     */
    boolean exchangeCodeForToken(String code, User user);

    /**
     * Check if the user has authorized Microsoft Calendar access.
     *
     * @param user the user
     * @return true if authorized, false otherwise
     */
    boolean isAuthorized(User user);

    /**
     * Synchronize events with Microsoft Calendar.
     *
     * @param user the user
     * @return the list of synchronized event DTOs
     */
    List<CalendarEventDTO> synchronizeEvents(User user);

    /**
     * Create an event in Microsoft Calendar.
     *
     * @param eventDTO the event DTO
     * @param user the user
     * @return the external event ID
     */
    String createEvent(CalendarEventDTO eventDTO, User user);

    /**
     * Update an event in Microsoft Calendar.
     *
     * @param eventDTO the event DTO
     * @param user the user
     * @return true if successful, false otherwise
     */
    boolean updateEvent(CalendarEventDTO eventDTO, User user);

    /**
     * Delete an event from Microsoft Calendar.
     *
     * @param eventDTO the event DTO
     * @param user the user
     * @return true if successful, false otherwise
     */
    boolean deleteEvent(CalendarEventDTO eventDTO, User user);
}