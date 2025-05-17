package pl.where2play.w2papi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.where2play.w2papi.dto.CalendarEventDTO;
import pl.where2play.w2papi.dto.request.CreateCalendarEventRequest;
import pl.where2play.w2papi.dto.request.UpdateCalendarEventRequest;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.CalendarEventRepository;
import pl.where2play.w2papi.repository.UserRepository;
import pl.where2play.w2papi.service.impl.CalendarEventServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarEventServiceTest {

    @Mock
    private CalendarEventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GoogleCalendarService googleCalendarService;

    @InjectMocks
    private CalendarEventServiceImpl calendarEventService;

    private User owner;
    private User attendee;
    private CalendarEvent event;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        owner = User.builder()
                .id(1L)
                .email("owner@example.com")
                .name("Event Owner")
                .provider(User.AuthProvider.GOOGLE)
                .providerId("owner123")
                .build();

        attendee = User.builder()
                .id(2L)
                .email("attendee@example.com")
                .name("Event Attendee")
                .provider(User.AuthProvider.GOOGLE)
                .providerId("attendee123")
                .build();

        event = CalendarEvent.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startTime(now)
                .endTime(now.plusHours(1))
                .location("Test Location")
                .owner(owner)
                .attendees(new HashSet<>())
                .provider(CalendarEvent.CalendarProvider.GOOGLE)
                .externalId("google123")
                .build();
    }

    @Test
    void testCreateEvent() {
        // Given
        CreateCalendarEventRequest request = CreateCalendarEventRequest.builder()
                .title("New Event")
                .description("New Description")
                .startTime(now)
                .endTime(now.plusHours(2))
                .location("New Location")
                .attendeeIds(Set.of(2L))
                .calendarProvider("GOOGLE")
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(attendee));
        when(googleCalendarService.createEvent(any(CalendarEvent.class), eq(owner)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(eventRepository.save(any(CalendarEvent.class))).thenAnswer(invocation -> {
            CalendarEvent savedEvent = invocation.getArgument(0);
            savedEvent.setId(1L);
            return savedEvent;
        });

        // When
        CalendarEventDTO result = calendarEventService.createEvent(request, owner);

        // Then
        assertNotNull(result);
        assertEquals("New Event", result.getTitle());
        assertEquals("New Description", result.getDescription());
        assertEquals(now, result.getStartTime());
        assertEquals(now.plusHours(2), result.getEndTime());
        assertEquals("New Location", result.getLocation());
        assertEquals("GOOGLE", result.getProvider());
        assertEquals(1, result.getAttendees().size());

        verify(eventRepository).save(any(CalendarEvent.class));
        verify(googleCalendarService).createEvent(any(CalendarEvent.class), eq(owner));
    }

    @Test
    void testUpdateEvent() {
        // Given
        UpdateCalendarEventRequest request = UpdateCalendarEventRequest.builder()
                .title("Updated Event")
                .description("Updated Description")
                .startTime(now.plusHours(1))
                .endTime(now.plusHours(3))
                .location("Updated Location")
                .attendeeIds(Set.of(2L))
                .build();

        when(eventRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(2L)).thenReturn(Optional.of(attendee));
        when(googleCalendarService.updateEvent(any(CalendarEvent.class), eq(owner)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(eventRepository.save(any(CalendarEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CalendarEventDTO result = calendarEventService.updateEvent(1L, request, owner);

        // Then
        assertNotNull(result);
        assertEquals("Updated Event", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(now.plusHours(1), result.getStartTime());
        assertEquals(now.plusHours(3), result.getEndTime());
        assertEquals("Updated Location", result.getLocation());
        assertEquals(1, result.getAttendees().size());

        verify(eventRepository).findByIdAndDeletedFalse(1L);
        verify(eventRepository).save(any(CalendarEvent.class));
        verify(googleCalendarService).updateEvent(any(CalendarEvent.class), eq(owner));
    }

    @Test
    void testDeleteEvent() {
        // Given
        when(eventRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(event));

        // When
        calendarEventService.deleteEvent(1L, owner);

        // Then
        verify(eventRepository).findByIdAndDeletedFalse(1L);
        verify(googleCalendarService).deleteEvent(event, owner);
        verify(eventRepository).save(event);
        assertTrue(event.isDeleted());
        assertNotNull(event.getDeletedAt());
    }

    @Test
    void testGetEvent() {
        // Given
        when(eventRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(event));

        // When
        CalendarEventDTO result = calendarEventService.getEvent(1L, owner);

        // Then
        assertNotNull(result);
        assertEquals(event.getId(), result.getId());
        assertEquals(event.getTitle(), result.getTitle());
        assertEquals(event.getDescription(), result.getDescription());
        assertEquals(event.getStartTime(), result.getStartTime());
        assertEquals(event.getEndTime(), result.getEndTime());
        assertEquals(event.getLocation(), result.getLocation());
        assertEquals(event.getProvider().name(), result.getProvider());

        verify(eventRepository).findByIdAndDeletedFalse(1L);
    }

    @Test
    void testGetAllEventsForUser() {
        // Given
        CalendarEvent event2 = CalendarEvent.builder()
                .id(2L)
                .title("Test Event 2")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(1).plusHours(1))
                .owner(owner)
                .build();

        when(eventRepository.findAllEventsForUser(owner)).thenReturn(Arrays.asList(event, event2));

        // When
        List<CalendarEventDTO> results = calendarEventService.getAllEventsForUser(owner);

        // Then
        assertEquals(2, results.size());
        assertEquals(event.getId(), results.get(0).getId());
        assertEquals(event2.getId(), results.get(1).getId());

        verify(eventRepository).findAllEventsForUser(owner);
    }

    @Test
    void testGetEventsInDateRange() {
        // Given
        LocalDateTime start = now.minusHours(1);
        LocalDateTime end = now.plusHours(2);

        when(eventRepository.findAllEventsForUserInDateRange(owner, start, end))
                .thenReturn(List.of(event));

        // When
        List<CalendarEventDTO> results = calendarEventService.getEventsInDateRange(owner, start, end);

        // Then
        assertEquals(1, results.size());
        assertEquals(event.getId(), results.get(0).getId());

        verify(eventRepository).findAllEventsForUserInDateRange(owner, start, end);
    }

    @Test
    void testAddAttendee() {
        // Given
        when(eventRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(2L)).thenReturn(Optional.of(attendee));
        when(googleCalendarService.updateEvent(any(CalendarEvent.class), eq(owner)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(eventRepository.save(any(CalendarEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CalendarEventDTO result = calendarEventService.addAttendee(1L, 2L, owner);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getAttendees().size());

        verify(eventRepository).findByIdAndDeletedFalse(1L);
        verify(userRepository).findById(2L);
        verify(googleCalendarService).updateEvent(any(CalendarEvent.class), eq(owner));
        verify(eventRepository).save(any(CalendarEvent.class));
    }

    @Test
    void testRemoveAttendee() {
        // Given
        event.getAttendees().add(attendee);

        when(eventRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(2L)).thenReturn(Optional.of(attendee));
        when(googleCalendarService.updateEvent(any(CalendarEvent.class), eq(owner)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(eventRepository.save(any(CalendarEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CalendarEventDTO result = calendarEventService.removeAttendee(1L, 2L, owner);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getAttendees().size());

        verify(eventRepository).findByIdAndDeletedFalse(1L);
        verify(userRepository).findById(2L);
        verify(googleCalendarService).updateEvent(any(CalendarEvent.class), eq(owner));
        verify(eventRepository).save(any(CalendarEvent.class));
    }

    @Test
    void testSynchronizeEvents() {
        // Given
        when(googleCalendarService.synchronizeEvents(owner)).thenReturn(List.of(event));

        // When
        List<CalendarEventDTO> results = calendarEventService.synchronizeEvents(owner, CalendarEvent.CalendarProvider.GOOGLE);

        // Then
        assertEquals(1, results.size());
        assertEquals(event.getId(), results.get(0).getId());

        verify(googleCalendarService).synchronizeEvents(owner);
    }

    @Test
    void testSynchronizeEventsUnsupportedProvider() {
        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> 
                calendarEventService.synchronizeEvents(owner, CalendarEvent.CalendarProvider.LOCAL));
    }

    @Test
    void testHardDeleteEvent() {
        // Given
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        // When
        calendarEventService.hardDeleteEvent(1L, owner, true);

        // Then
        verify(eventRepository).findById(1L);
        verify(googleCalendarService).deleteEvent(event, owner);
        verify(eventRepository).delete(event);
    }

    @Test
    void testHardDeleteEventNotE2ETest() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                calendarEventService.hardDeleteEvent(1L, owner, false));
    }
}
