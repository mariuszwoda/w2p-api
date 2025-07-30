package pl.where2play.w2papi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a shared calendar event in the system.
 * This allows calendar events to be shared with other users with different permission levels.
 */
@Entity
@Table(name = "calendar_shares")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CalendarEvent event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    /**
     * Email address for sharing with users not registered in the system.
     * This field is used when user is null.
     */
    @Column(name = "shared_email")
    private String sharedEmail;

    @Column(name = "permission_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private PermissionLevel permissionLevel;

    @Column(name = "share_token")
    private String shareToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum representing the permission levels for shared calendars.
     */
    public enum PermissionLevel {
        VIEW,   // Can only view the event
        EDIT,   // Can view and edit the event
        ADMIN   // Can view, edit, and manage sharing of the event
    }
}