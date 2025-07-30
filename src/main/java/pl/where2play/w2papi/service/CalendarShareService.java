package pl.where2play.w2papi.service;

import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.CalendarShare;
import pl.where2play.w2papi.model.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing calendar shares.
 */
public interface CalendarShareService {

    /**
     * Share an event with a user.
     *
     * @param event the event to share
     * @param user the user to share with
     * @param permissionLevel the permission level
     * @param expiresAt the expiration date (optional)
     * @return the created share
     */
    CalendarShare shareEventWithUser(CalendarEvent event, User user, CalendarShare.PermissionLevel permissionLevel, LocalDateTime expiresAt);

    /**
     * Share an event with an email address.
     *
     * @param event the event to share
     * @param email the email to share with
     * @param permissionLevel the permission level
     * @param expiresAt the expiration date (optional)
     * @return the created share
     */
    CalendarShare shareEventWithEmail(CalendarEvent event, String email, CalendarShare.PermissionLevel permissionLevel, LocalDateTime expiresAt);

    /**
     * Get all shares for an event.
     *
     * @param event the event
     * @return the list of shares
     */
    List<CalendarShare> getSharesForEvent(CalendarEvent event);

    /**
     * Get all events shared with a user.
     *
     * @param user the user
     * @return the list of events
     */
    List<CalendarEvent> getEventsSharedWithUser(User user);

    /**
     * Get all events shared with an email.
     *
     * @param email the email
     * @return the list of events
     */
    List<CalendarEvent> getEventsSharedWithEmail(String email);

    /**
     * Update a share's permission level.
     *
     * @param shareId the share ID
     * @param permissionLevel the new permission level
     * @param currentUser the current user (must be the event owner)
     * @return the updated share
     */
    CalendarShare updateSharePermission(Long shareId, CalendarShare.PermissionLevel permissionLevel, User currentUser);

    /**
     * Update a share's expiration date.
     *
     * @param shareId the share ID
     * @param expiresAt the new expiration date
     * @param currentUser the current user (must be the event owner)
     * @return the updated share
     */
    CalendarShare updateShareExpiration(Long shareId, LocalDateTime expiresAt, User currentUser);

    /**
     * Delete a share.
     *
     * @param shareId the share ID
     * @param currentUser the current user (must be the event owner)
     */
    void deleteShare(Long shareId, User currentUser);

    /**
     * Get a share by its token.
     *
     * @param shareToken the share token
     * @return the share
     */
    CalendarShare getShareByToken(String shareToken);

    /**
     * Generate a new share token for a share.
     *
     * @param shareId the share ID
     * @param currentUser the current user (must be the event owner)
     * @return the updated share with a new token
     */
    CalendarShare regenerateShareToken(Long shareId, User currentUser);
}