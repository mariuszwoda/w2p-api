package pl.where2play.w2papi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.CalendarShare;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.CalendarEventRepository;
import pl.where2play.w2papi.repository.CalendarShareRepository;
import pl.where2play.w2papi.service.CalendarShareService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the CalendarShareService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarShareServiceImpl implements CalendarShareService {

    private final CalendarShareRepository shareRepository;
    private final CalendarEventRepository eventRepository;

    @Override
    @Transactional
    public CalendarShare shareEventWithUser(CalendarEvent event, User user, CalendarShare.PermissionLevel permissionLevel, LocalDateTime expiresAt) {
        log.info("Sharing event with ID: {} with user ID: {}", event.getId(), user.getId());
        
        // Check if share already exists
        var existingShare = shareRepository.findByEventAndUser(event, user);
        if (existingShare.isPresent()) {
            log.info("Share already exists, updating permission level and expiration");
            CalendarShare share = existingShare.get();
            share.setPermissionLevel(permissionLevel);
            share.setExpiresAt(expiresAt);
            return shareRepository.save(share);
        }
        
        // Create new share
        CalendarShare share = CalendarShare.builder()
                .event(event)
                .user(user)
                .permissionLevel(permissionLevel)
                .expiresAt(expiresAt)
                .shareToken(generateShareToken())
                .build();
        
        return shareRepository.save(share);
    }

    @Override
    @Transactional
    public CalendarShare shareEventWithEmail(CalendarEvent event, String email, CalendarShare.PermissionLevel permissionLevel, LocalDateTime expiresAt) {
        log.info("Sharing event with ID: {} with email: {}", event.getId(), email);
        
        // Check if share already exists
        var existingShare = shareRepository.findByEventAndSharedEmail(event, email);
        if (existingShare.isPresent()) {
            log.info("Share already exists, updating permission level and expiration");
            CalendarShare share = existingShare.get();
            share.setPermissionLevel(permissionLevel);
            share.setExpiresAt(expiresAt);
            return shareRepository.save(share);
        }
        
        // Create new share
        CalendarShare share = CalendarShare.builder()
                .event(event)
                .sharedEmail(email)
                .permissionLevel(permissionLevel)
                .expiresAt(expiresAt)
                .shareToken(generateShareToken())
                .build();
        
        return shareRepository.save(share);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarShare> getSharesForEvent(CalendarEvent event) {
        log.info("Getting shares for event with ID: {}", event.getId());
        return shareRepository.findByEvent(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEvent> getEventsSharedWithUser(User user) {
        log.info("Getting events shared with user ID: {}", user.getId());
        return shareRepository.findAllEventsSharedWithUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEvent> getEventsSharedWithEmail(String email) {
        log.info("Getting events shared with email: {}", email);
        return shareRepository.findAllEventsSharedWithEmail(email);
    }

    @Override
    @Transactional
    public CalendarShare updateSharePermission(Long shareId, CalendarShare.PermissionLevel permissionLevel, User currentUser) {
        log.info("Updating permission level for share with ID: {} to: {}", shareId, permissionLevel);
        
        CalendarShare share = getShareAndVerifyOwnership(shareId, currentUser);
        share.setPermissionLevel(permissionLevel);
        return shareRepository.save(share);
    }

    @Override
    @Transactional
    public CalendarShare updateShareExpiration(Long shareId, LocalDateTime expiresAt, User currentUser) {
        log.info("Updating expiration date for share with ID: {} to: {}", shareId, expiresAt);
        
        CalendarShare share = getShareAndVerifyOwnership(shareId, currentUser);
        share.setExpiresAt(expiresAt);
        return shareRepository.save(share);
    }

    @Override
    @Transactional
    public void deleteShare(Long shareId, User currentUser) {
        log.info("Deleting share with ID: {}", shareId);
        
        CalendarShare share = getShareAndVerifyOwnership(shareId, currentUser);
        shareRepository.delete(share);
    }

    @Override
    @Transactional(readOnly = true)
    public CalendarShare getShareByToken(String shareToken) {
        log.info("Getting share by token: {}", shareToken);
        
        return shareRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new IllegalArgumentException("Share not found with token: " + shareToken));
    }

    @Override
    @Transactional
    public CalendarShare regenerateShareToken(Long shareId, User currentUser) {
        log.info("Regenerating token for share with ID: {}", shareId);
        
        CalendarShare share = getShareAndVerifyOwnership(shareId, currentUser);
        share.setShareToken(generateShareToken());
        return shareRepository.save(share);
    }
    
    /**
     * Get a share and verify that the current user is the owner of the event.
     *
     * @param shareId the share ID
     * @param currentUser the current user
     * @return the share
     * @throws IllegalArgumentException if the share is not found or the user is not the owner
     */
    private CalendarShare getShareAndVerifyOwnership(Long shareId, User currentUser) {
        CalendarShare share = shareRepository.findById(shareId)
                .orElseThrow(() -> new IllegalArgumentException("Share not found with ID: " + shareId));
        
        if (!share.getEvent().getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("User is not the owner of the event");
        }
        
        return share;
    }
    
    /**
     * Generate a unique share token.
     *
     * @return the share token
     */
    private String generateShareToken() {
        return UUID.randomUUID().toString();
    }
}