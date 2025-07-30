package pl.where2play.w2papi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.EventNotification;
import pl.where2play.w2papi.model.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for EventNotification entity.
 */
@Repository
public interface EventNotificationRepository extends JpaRepository<EventNotification, Long> {

    /**
     * Find all notifications for a user.
     *
     * @param user the user
     * @return the list of notifications
     */
    List<EventNotification> findByUser(User user);

    /**
     * Find all notifications for an event.
     *
     * @param event the event
     * @return the list of notifications
     */
    List<EventNotification> findByEvent(CalendarEvent event);

    /**
     * Find all pending notifications that are due to be sent.
     *
     * @param now the current time
     * @return the list of due notifications
     */
    @Query("SELECT n FROM EventNotification n WHERE n.status = 'PENDING' AND n.notificationTime <= :now")
    List<EventNotification> findDueNotifications(@Param("now") LocalDateTime now);

    /**
     * Find all notifications for an event and a user.
     *
     * @param event the event
     * @param user the user
     * @return the list of notifications
     */
    List<EventNotification> findByEventAndUser(CalendarEvent event, User user);

    /**
     * Find all notifications for an event with a specific status.
     *
     * @param event the event
     * @param status the status
     * @return the list of notifications
     */
    List<EventNotification> findByEventAndStatus(CalendarEvent event, EventNotification.NotificationStatus status);

    /**
     * Find all notifications for a user with a specific status.
     *
     * @param user the user
     * @param status the status
     * @return the list of notifications
     */
    List<EventNotification> findByUserAndStatus(User user, EventNotification.NotificationStatus status);

    /**
     * Count all notifications for an event.
     *
     * @param event the event
     * @return the number of notifications
     */
    long countByEvent(CalendarEvent event);

    /**
     * Count all notifications for a user.
     *
     * @param user the user
     * @return the number of notifications
     */
    long countByUser(User user);

    /**
     * Delete all notifications for an event.
     *
     * @param event the event
     * @return the number of notifications deleted
     */
    long deleteByEvent(CalendarEvent event);
}