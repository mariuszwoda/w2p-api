package pl.where2play.w2papi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.where2play.w2papi.model.CalendarEvent;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for CalendarEvent entity.
 * Used for transferring calendar event data between client and server.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private UserDTO owner;
    private Set<UserDTO> attendees;
    private String externalId;
    private String provider;
    private boolean allDay;
    private String recurrenceRule;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts a CalendarEvent entity to a CalendarEventDTO.
     *
     * @param event the CalendarEvent entity to convert
     * @return the CalendarEventDTO
     */
    public static CalendarEventDTO fromEntity(CalendarEvent event) {
        if (event == null) {
            return null;
        }
        
        return CalendarEventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .owner(UserDTO.fromEntity(event.getOwner()))
                .attendees(event.getAttendees() != null 
                        ? event.getAttendees().stream()
                            .map(UserDTO::fromEntity)
                            .collect(Collectors.toSet())
                        : null)
                .externalId(event.getExternalId())
                .provider(event.getProvider() != null ? event.getProvider().name() : null)
                .allDay(event.isAllDay())
                .recurrenceRule(event.getRecurrenceRule())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}