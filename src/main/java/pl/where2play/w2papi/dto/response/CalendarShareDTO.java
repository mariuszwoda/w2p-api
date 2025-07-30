package pl.where2play.w2papi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.where2play.w2papi.model.CalendarShare;

import java.time.LocalDateTime;

/**
 * Response DTO for calendar shares.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarShareDTO {

    /**
     * The ID of the share.
     */
    private Long id;

    /**
     * The ID of the shared event.
     */
    private Long eventId;

    /**
     * The title of the shared event.
     */
    private String eventTitle;

    /**
     * The ID of the user the event is shared with.
     * This is null if the event is shared with an email address.
     */
    private Long userId;

    /**
     * The name of the user the event is shared with.
     * This is null if the event is shared with an email address.
     */
    private String userName;

    /**
     * The email address the event is shared with.
     * This is null if the event is shared with a registered user.
     */
    private String sharedEmail;

    /**
     * The permission level for the share.
     */
    private CalendarShare.PermissionLevel permissionLevel;

    /**
     * The token for accessing the shared event.
     */
    private String shareToken;

    /**
     * The expiration date for the share.
     * If null, the share does not expire.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    /**
     * The date the share was created.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Converts a CalendarShare entity to a CalendarShareDTO.
     *
     * @param share the CalendarShare entity
     * @return the CalendarShareDTO
     */
    public static CalendarShareDTO fromEntity(CalendarShare share) {
        return CalendarShareDTO.builder()
                .id(share.getId())
                .eventId(share.getEvent().getId())
                .eventTitle(share.getEvent().getTitle())
                .userId(share.getUser() != null ? share.getUser().getId() : null)
                .userName(share.getUser() != null ? share.getUser().getName() : null)
                .sharedEmail(share.getSharedEmail())
                .permissionLevel(share.getPermissionLevel())
                .shareToken(share.getShareToken())
                .expiresAt(share.getExpiresAt())
                .createdAt(share.getCreatedAt())
                .build();
    }
}