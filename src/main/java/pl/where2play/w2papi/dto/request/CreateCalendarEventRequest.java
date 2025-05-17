package pl.where2play.w2papi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Request DTO for creating a new calendar event.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCalendarEventRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    private String location;
    
    private Set<Long> attendeeIds;
    
    private boolean allDay;
    
    private String recurrenceRule;
    
    private String calendarProvider;
}