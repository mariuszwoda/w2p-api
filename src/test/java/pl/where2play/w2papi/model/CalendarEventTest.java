package pl.where2play.w2papi.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashSet;

class CalendarEventTest {

    @Test
    void testCalendarEventCreation() {
        // Given
        String title = "Test Event";
        String description = "This is a test event";
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(1);
        String location = "Test Location";
        boolean allDay = false;
        String recurrenceRule = "FREQ=DAILY;COUNT=5";
        CalendarEvent.CalendarProvider provider = CalendarEvent.CalendarProvider.GOOGLE;
        String externalId = "google123";
        
        User owner = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        // When
        CalendarEvent event = CalendarEvent.builder()
                .title(title)
                .description(description)
                .startTime(startTime)
                .endTime(endTime)
                .location(location)
                .owner(owner)
                .allDay(allDay)
                .recurrenceRule(recurrenceRule)
                .provider(provider)
                .externalId(externalId)
                .attendees(new HashSet<>())
                .build();

        // Then
        assertNotNull(event);
        assertEquals(title, event.getTitle());
        assertEquals(description, event.getDescription());
        assertEquals(startTime, event.getStartTime());
        assertEquals(endTime, event.getEndTime());
        assertEquals(location, event.getLocation());
        assertEquals(owner, event.getOwner());
        assertEquals(allDay, event.isAllDay());
        assertEquals(recurrenceRule, event.getRecurrenceRule());
        assertEquals(provider, event.getProvider());
        assertEquals(externalId, event.getExternalId());
        assertNotNull(event.getAttendees());
        assertTrue(event.getAttendees().isEmpty());
    }

    @Test
    void testCalendarEventEquality() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        CalendarEvent event1 = CalendarEvent.builder()
                .id(1L)
                .title("Same Event")
                .startTime(now)
                .endTime(now.plusHours(1))
                .build();

        CalendarEvent event2 = CalendarEvent.builder()
                .id(1L)
                .title("Same Event")
                .startTime(now)
                .endTime(now.plusHours(1))
                .build();

        CalendarEvent event3 = CalendarEvent.builder()
                .id(2L)
                .title("Different Event")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(1).plusHours(1))
                .build();

        // Then
        assertEquals(event1, event2);
        assertNotEquals(event1, event3);
    }

    @Test
    void testAddAttendee() {
        // Given
        CalendarEvent event = CalendarEvent.builder()
                .title("Test Event")
                .attendees(new HashSet<>())
                .build();

        User attendee = User.builder()
                .id(1L)
                .email("attendee@example.com")
                .build();

        // When
        event.getAttendees().add(attendee);

        // Then
        assertEquals(1, event.getAttendees().size());
        assertTrue(event.getAttendees().contains(attendee));
    }

    @Test
    void testAllDayEvent() {
        // Given
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime tomorrow = today.plusDays(1);

        // When
        CalendarEvent event = CalendarEvent.builder()
                .title("All Day Event")
                .startTime(today)
                .endTime(tomorrow)
                .allDay(true)
                .build();

        // Then
        assertTrue(event.isAllDay());
        assertEquals(today, event.getStartTime());
        assertEquals(tomorrow, event.getEndTime());
        assertEquals(24, java.time.Duration.between(event.getStartTime(), event.getEndTime()).toHours());
    }
}