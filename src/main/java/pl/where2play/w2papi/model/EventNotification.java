package pl.where2play.w2papi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a notification for a calendar event.
 * This allows for sending reminders to users about upcoming events.
 */
@Entity
@Table(name = "event_notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CalendarEvent event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    /**
     * The time before the event when the notification should be sent.
     * For example, 15 minutes, 1 hour, 1 day, etc.
     */
    @Column(name = "minutes_before", nullable = false)
    private int minutesBefore;

    /**
     * The time when the notification should be sent.
     * This is calculated as event.startTime - minutesBefore.
     */
    @Column(name = "notification_time", nullable = false)
    private LocalDateTime notificationTime;

    /**
     * The time when the notification was sent.
     * If null, the notification has not been sent yet.
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * The status of the notification.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status;

    /**
     * The channel through which the notification should be sent.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;

    /**
     * The content of the notification.
     * This can be customized by the user.
     */
    @Column(name = "content", length = 1000)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum representing the status of a notification.
     */
    public enum NotificationStatus {
        PENDING,
        SENT,
        FAILED,
        CANCELLED
    }

    /**
     * Enum representing the channel through which a notification should be sent.
     */
    public enum NotificationChannel {
        EMAIL,
        SMS,
        PUSH,
        IN_APP
    }

    /**
     * Calculate the notification time based on the event start time and minutes before.
     *
     * @return the notification time
     */
    public LocalDateTime calculateNotificationTime() {
        return event.getStartTime().minusMinutes(minutesBefore);
    }

    /**
     * Check if the notification is due to be sent.
     *
     * @param now the current time
     * @return true if the notification is due, false otherwise
     */
    public boolean isDue(LocalDateTime now) {
        return status == NotificationStatus.PENDING && 
               notificationTime.isBefore(now) && 
               (sentAt == null || sentAt.isAfter(now));
    }

    /**
     * Mark the notification as sent.
     *
     * @param sentAt the time when the notification was sent
     */
    public void markAsSent(LocalDateTime sentAt) {
        this.sentAt = sentAt;
        this.status = NotificationStatus.SENT;
    }

    /**
     * Mark the notification as failed.
     */
    public void markAsFailed() {
        this.status = NotificationStatus.FAILED;
    }

    /**
     * Cancel the notification.
     */
    public void cancel() {
        this.status = NotificationStatus.CANCELLED;
    }
}