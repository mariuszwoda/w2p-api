package pl.where2play.w2papi.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.where2play.w2papi.model.CalendarShare;

import java.time.LocalDateTime;

/**
 * Request DTO for sharing a calendar event.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareEventRequest {

    /**
     * The ID of the event to share.
     */
    @NotNull(message = "Event ID is required")
    private Long eventId;

    /**
     * The ID of the user to share with.
     * This is used when sharing with a registered user.
     * Either recipientId or recipientEmail must be provided.
     */
    private Long recipientId;

    /**
     * The email address to share with.
     * This is used when sharing with a user not registered in the system.
     * Either recipientId or recipientEmail must be provided.
     */
    @Email(message = "Invalid email format")
    private String recipientEmail;

    /**
     * The permission level for the share.
     */
    @NotNull(message = "Permission level is required")
    private CalendarShare.PermissionLevel permissionLevel;

    /**
     * The expiration date for the share.
     * If not provided, the share will not expire.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
}