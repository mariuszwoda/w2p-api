package pl.where2play.w2papi.service;

import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.EventNotification;
import pl.where2play.w2papi.model.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing event notifications.
 */
public interface NotificationService {

    /**
     * Create a notification for an event.
     *
     * @param event the event
     * @param user the user to notify
     * @param minutesBefore the time before the event when the notification should be sent
     * @param channel the notification channel
     * @param content the notification content (optional)
     * @return the created notification
     */
    EventNotification createNotification(CalendarEvent event, User user, int minutesBefore, 
                                        EventNotification.NotificationChannel channel, String content);

    /**
     * Get all notifications for a user.
     *
     * @param user the user
     * @return the list of notifications
     */
    List<EventNotification> getNotificationsForUser(User user);

    /**
     * Get all notifications for an event.
     *
     * @param event the event
     * @return the list of notifications
     */
    List<EventNotification> getNotificationsForEvent(CalendarEvent event);

    /**
     * Get all pending notifications that are due to be sent.
     *
     * @param now the current time
     * @return the list of due notifications
     */
    List<EventNotification> getDueNotifications(LocalDateTime now);

    /**
     * Send a notification.
     *
     * @param notification the notification to send
     * @return true if the notification was sent successfully, false otherwise
     */
    boolean sendNotification(EventNotification notification);

    /**
     * Send all due notifications.
     *
     * @param now the current time
     * @return the number of notifications sent
     */
    int sendDueNotifications(LocalDateTime now);

    /**
     * Cancel a notification.
     *
     * @param notification the notification to cancel
     */
    void cancelNotification(EventNotification notification);

    /**
     * Cancel all notifications for an event.
     *
     * @param event the event
     * @return the number of notifications cancelled
     */
    int cancelNotificationsForEvent(CalendarEvent event);

    /**
     * Update notification times for an event.
     * This should be called when an event's start time is updated.
     *
     * @param event the event
     * @return the number of notifications updated
     */
    int updateNotificationTimesForEvent(CalendarEvent event);

    /**
     * Get default notification times for a user.
     * These are the times before an event when notifications should be sent by default.
     *
     * @param user the user
     * @return the list of default notification times in minutes
     */
    List<Integer> getDefaultNotificationTimes(User user);

    /**
     * Set default notification times for a user.
     *
     * @param user the user
     * @param minutesBeforeList the list of default notification times in minutes
     */
    void setDefaultNotificationTimes(User user, List<Integer> minutesBeforeList);

    /**
     * Get default notification channel for a user.
     *
     * @param user the user
     * @return the default notification channel
     */
    EventNotification.NotificationChannel getDefaultNotificationChannel(User user);

    /**
     * Set default notification channel for a user.
     *
     * @param user the user
     * @param channel the default notification channel
     */
    void setDefaultNotificationChannel(User user, EventNotification.NotificationChannel channel);
}