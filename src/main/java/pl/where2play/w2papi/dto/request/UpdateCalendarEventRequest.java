package pl.where2play.w2papi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Request DTO for updating an existing calendar event.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCalendarEventRequest {
    
    private String title;
    
    private String description;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private String location;
    
    private Set<Long> attendeeIds;
    
    private Boolean allDay;
    
    private String recurrenceRule;
    
    private String calendarProvider;
}