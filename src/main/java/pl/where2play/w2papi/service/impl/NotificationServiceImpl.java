package pl.where2play.w2papi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.EventNotification;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.EventNotificationRepository;
import pl.where2play.w2papi.repository.UserRepository;
import pl.where2play.w2papi.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of the NotificationService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final EventNotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // Default notification times in minutes (15 minutes, 1 hour, 1 day)
    private static final List<Integer> DEFAULT_NOTIFICATION_TIMES = Arrays.asList(15, 60, 1440);
    private static final EventNotification.NotificationChannel DEFAULT_NOTIFICATION_CHANNEL = EventNotification.NotificationChannel.EMAIL;

    @Override
    @Transactional
    public EventNotification createNotification(CalendarEvent event, User user, int minutesBefore,
                                               EventNotification.NotificationChannel channel, String content) {
        log.info("Creating notification for event with ID: {} for user: {} ({} minutes before)", 
                event.getId(), user.getEmail(), minutesBefore);
        
        // Calculate notification time
        LocalDateTime notificationTime = event.getStartTime().minusMinutes(minutesBefore);
        
        // Create notification
        EventNotification notification = EventNotification.builder()
                .event(event)
                .user(user)
                .minutesBefore(minutesBefore)
                .notificationTime(notificationTime)
                .status(EventNotification.NotificationStatus.PENDING)
                .channel(channel)
                .content(content)
                .build();
        
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventNotification> getNotificationsForUser(User user) {
        log.info("Getting notifications for user: {}", user.getEmail());
        return notificationRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventNotification> getNotificationsForEvent(CalendarEvent event) {
        log.info("Getting notifications for event with ID: {}", event.getId());
        return notificationRepository.findByEvent(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventNotification> getDueNotifications(LocalDateTime now) {
        log.info("Getting due notifications at: {}", now);
        return notificationRepository.findDueNotifications(now);
    }

    @Override
    @Transactional
    public boolean sendNotification(EventNotification notification) {
        log.info("Sending notification with ID: {}", notification.getId());
        
        try {
            switch (notification.getChannel()) {
                case EMAIL:
                    sendEmailNotification(notification);
                    break;
                case SMS:
                    sendSmsNotification(notification);
                    break;
                case PUSH:
                    sendPushNotification(notification);
                    break;
                case IN_APP:
                    sendInAppNotification(notification);
                    break;
                default:
                    log.warn("Unsupported notification channel: {}", notification.getChannel());
                    return false;
            }
            
            // Mark notification as sent
            notification.markAsSent(LocalDateTime.now());
            notificationRepository.save(notification);
            
            return true;
        } catch (Exception e) {
            log.error("Failed to send notification with ID: {}", notification.getId(), e);
            notification.markAsFailed();
            notificationRepository.save(notification);
            return false;
        }
    }

    private void sendEmailNotification(EventNotification notification) {
        log.info("Sending email notification to: {}", notification.getUser().getEmail());
        
        String content = notification.getContent();
        if (content == null || content.isEmpty()) {
            content = generateDefaultContent(notification);
        }
        
        // TODO: Implement actual email sending using a proper email service
        // For now, just log the email content
        log.info("Email subject: Reminder: {}", notification.getEvent().getTitle());
        log.info("Email content: {}", content);
    }

    private void sendSmsNotification(EventNotification notification) {
        log.info("Sending SMS notification (not implemented yet)");
        // TODO: Implement SMS notification
    }

    private void sendPushNotification(EventNotification notification) {
        log.info("Sending push notification (not implemented yet)");
        // TODO: Implement push notification
    }

    private void sendInAppNotification(EventNotification notification) {
        log.info("Sending in-app notification (not implemented yet)");
        // TODO: Implement in-app notification
    }

    private String generateDefaultContent(EventNotification notification) {
        CalendarEvent event = notification.getEvent();
        return String.format(
                "Reminder: %s\n\nEvent: %s\nTime: %s\nLocation: %s\n\nDescription: %s",
                formatMinutesBefore(notification.getMinutesBefore()),
                event.getTitle(),
                event.getStartTime(),
                event.getLocation() != null ? event.getLocation() : "Not specified",
                event.getDescription() != null ? event.getDescription() : "No description"
        );
    }

    private String formatMinutesBefore(int minutesBefore) {
        if (minutesBefore < 60) {
            return minutesBefore + " minutes before the event";
        } else if (minutesBefore == 60) {
            return "1 hour before the event";
        } else if (minutesBefore < 1440) {
            return (minutesBefore / 60) + " hours before the event";
        } else if (minutesBefore == 1440) {
            return "1 day before the event";
        } else {
            return (minutesBefore / 1440) + " days before the event";
        }
    }

    @Override
    @Transactional
    public int sendDueNotifications(LocalDateTime now) {
        log.info("Sending due notifications at: {}", now);
        
        List<EventNotification> dueNotifications = getDueNotifications(now);
        int sentCount = 0;
        
        for (EventNotification notification : dueNotifications) {
            if (sendNotification(notification)) {
                sentCount++;
            }
        }
        
        log.info("Sent {} out of {} due notifications", sentCount, dueNotifications.size());
        return sentCount;
    }

    @Override
    @Transactional
    public void cancelNotification(EventNotification notification) {
        log.info("Cancelling notification with ID: {}", notification.getId());
        
        notification.cancel();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public int cancelNotificationsForEvent(CalendarEvent event) {
        log.info("Cancelling notifications for event with ID: {}", event.getId());
        
        List<EventNotification> notifications = notificationRepository.findByEventAndStatus(
                event, EventNotification.NotificationStatus.PENDING);
        
        for (EventNotification notification : notifications) {
            notification.cancel();
            notificationRepository.save(notification);
        }
        
        return notifications.size();
    }

    @Override
    @Transactional
    public int updateNotificationTimesForEvent(CalendarEvent event) {
        log.info("Updating notification times for event with ID: {}", event.getId());
        
        List<EventNotification> notifications = notificationRepository.findByEvent(event);
        int updatedCount = 0;
        
        for (EventNotification notification : notifications) {
            LocalDateTime newNotificationTime = event.getStartTime().minusMinutes(notification.getMinutesBefore());
            notification.setNotificationTime(newNotificationTime);
            notificationRepository.save(notification);
            updatedCount++;
        }
        
        return updatedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getDefaultNotificationTimes(User user) {
        log.info("Getting default notification times for user: {}", user.getEmail());
        
        // TODO: Store user preferences in the database
        return DEFAULT_NOTIFICATION_TIMES;
    }

    @Override
    @Transactional
    public void setDefaultNotificationTimes(User user, List<Integer> minutesBeforeList) {
        log.info("Setting default notification times for user: {}", user.getEmail());
        
        // TODO: Store user preferences in the database
    }

    @Override
    @Transactional(readOnly = true)
    public EventNotification.NotificationChannel getDefaultNotificationChannel(User user) {
        log.info("Getting default notification channel for user: {}", user.getEmail());
        
        // TODO: Store user preferences in the database
        return DEFAULT_NOTIFICATION_CHANNEL;
    }

    @Override
    @Transactional
    public void setDefaultNotificationChannel(User user, EventNotification.NotificationChannel channel) {
        log.info("Setting default notification channel for user: {}", user.getEmail());
        
        // TODO: Store user preferences in the database
    }
}