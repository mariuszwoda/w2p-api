package pl.where2play.w2papi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.where2play.w2papi.dto.CalendarEventDTO;
import pl.where2play.w2papi.dto.UserDTO;
import pl.where2play.w2papi.dto.request.CreateCalendarEventRequest;
import pl.where2play.w2papi.dto.request.UpdateCalendarEventRequest;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.service.CalendarEventService;
import pl.where2play.w2papi.service.UserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarEventControllerTest {

    @Mock
    private CalendarEventService calendarEventService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CalendarEventController calendarEventController;

    private CalendarEventDTO eventDTO;
    private User user;
    private UserDetails userDetails;
    private CreateCalendarEventRequest createRequest;
    private UpdateCalendarEventRequest updateRequest;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Set up test data
        UserDTO ownerDTO = UserDTO.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        eventDTO = CalendarEventDTO.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startTime(now)
                .endTime(now.plusHours(1))
                .location("Test Location")
                .owner(ownerDTO)
                .provider("GOOGLE")
                .build();

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .provider(User.AuthProvider.GOOGLE)
                .build();

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("test@example.com")
                .password("")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        createRequest = CreateCalendarEventRequest.builder()
                .title("New Event")
                .description("New Description")
                .startTime(now)
                .endTime(now.plusHours(2))
                .location("New Location")
                .attendeeIds(Set.of(2L))
                .calendarProvider("GOOGLE")
                .build();

        updateRequest = UpdateCalendarEventRequest.builder()
                .title("Updated Event")
                .description("Updated Description")
                .startTime(now.plusHours(1))
                .endTime(now.plusHours(3))
                .location("Updated Location")
                .attendeeIds(Set.of(2L))
                .build();
    }

    @Test
    void testCreateEvent() {
        // Given
        when(userService.getCurrentUser("test@example.com")).thenReturn(user);
        when(calendarEventService.createEvent(any(CreateCalendarEventRequest.class), eq(user))).thenReturn(eventDTO);

        // When
        ResponseEntity<CalendarEventDTO> response = calendarEventController.createEvent(createRequest, userDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test Event", response.getBody().getTitle());
        assertEquals("Test Description", response.getBody().getDescription());
        assertEquals("Test Location", response.getBody().getLocation());
        assertEquals("GOOGLE", response.getBody().getProvider());
        assertEquals(1L, response.getBody().getOwner().getId());
        assertEquals("test@example.com", response.getBody().getOwner().getEmail());

        verify(userService).getCurrentUser("test@example.com");
        verify(calendarEventService).createEvent(any(CreateCalendarEventRequest.class), eq(user));
    }

    @Test
    void testUpdateEvent() {
        // Given
        when(userService.getCurrentUser("test@example.com")).thenReturn(user);
        when(calendarEventService.updateEvent(eq(1L), any(UpdateCalendarEventRequest.class), eq(user))).thenReturn(eventDTO);

        // When
        ResponseEntity<CalendarEventDTO> response = calendarEventController.updateEvent(1L, updateRequest, userDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test Event", response.getBody().getTitle());
        assertEquals("Test Description", response.getBody().getDescription());
        assertEquals("Test Location", response.getBody().getLocation());
        assertEquals("GOOGLE", response.getBody().getProvider());

        verify(userService).getCurrentUser("test@example.com");
        verify(calendarEventService).updateEvent(eq(1L), any(UpdateCalendarEventRequest.class), eq(user));
    }

    @Test
    void testDeleteEvent() {
        // Given
        when(userService.getCurrentUser("test@example.com")).thenReturn(user);

        // When
        ResponseEntity<Void> response = calendarEventController.deleteEvent(1L, true, userDetails);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(userService).getCurrentUser("test@example.com");
        verify(calendarEventService).deleteEvent(1L, user);
    }

    @Test
    void testGetEvent() {
        // Given
        when(userService.getCurrentUser("test@example.com")).thenReturn(user);
        when(calendarEventService.getEvent(1L, user)).thenReturn(eventDTO);

        // When
        ResponseEntity<CalendarEventDTO> response = calendarEventController.getEvent(1L, userDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test Event", response.getBody().getTitle());
        assertEquals("Test Description", response.getBody().getDescription());
        assertEquals("Test Location", response.getBody().getLocation());
        assertEquals("GOOGLE", response.getBody().getProvider());

        verify(userService).getCurrentUser("test@example.com");
        verify(calendarEventService).getEvent(1L, user);
    }

    @Test
    void testGetAllEvents() {
        // Given
        when(userService.getCurrentUser("test@example.com")).thenReturn(user);

        CalendarEventDTO event2 = CalendarEventDTO.builder()
                .id(2L)
                .title("Test Event 2")
                .build();

        List<CalendarEventDTO> events = Arrays.asList(eventDTO, event2);

        when(calendarEventService.getAllEventsForUser(user)).thenReturn(events);

        // When
        ResponseEntity<List<CalendarEventDTO>> response = calendarEventController.getAllEvents(userDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("Test Event", response.getBody().get(0).getTitle());
        assertEquals(2L, response.getBody().get(1).getId());
        assertEquals("Test Event 2", response.getBody().get(1).getTitle());

        verify(userService).getCurrentUser("test@example.com");
        verify(calendarEventService).getAllEventsForUser(user);
    }

    @Test
    void testGetEventsInDateRange() {
        // Given
        LocalDateTime start = now.minusHours(1);
        LocalDateTime end = now.plusHours(2);

        when(userService.getCurrentUser("test@example.com")).thenReturn(user);
        when(calendarEventService.getEventsInDateRange(eq(user), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(eventDTO));

        // When
        ResponseEntity<List<CalendarEventDTO>> response = calendarEventController.getEventsInDateRange(start, end, userDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("Test Event", response.getBody().get(0).getTitle());

        verify(userService).getCurrentUser("test@example.com");
        verify(calendarEventService).getEventsInDateRange(eq(user), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testSynchronizeEvents() {
        // Given
        when(userService.getCurrentUser("test@example.com")).thenReturn(user);
        when(calendarEventService.synchronizeEvents(eq(user), eq(CalendarEvent.CalendarProvider.GOOGLE)))
                .thenReturn(List.of(eventDTO));

        // When
        ResponseEntity<List<CalendarEventDTO>> response = calendarEventController.synchronizeEvents("GOOGLE", userDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("Test Event", response.getBody().get(0).getTitle());

        verify(userService).getCurrentUser("test@example.com");
        verify(calendarEventService).synchronizeEvents(eq(user), eq(CalendarEvent.CalendarProvider.GOOGLE));
    }

    @Test
    void testAddAttendee() {
        // Given
        when(userService.getCurrentUser("test@example.com")).thenReturn(user);
        when(calendarEventService.addAttendee(1L, 2L, user)).thenReturn(eventDTO);

        // When
        ResponseEntity<CalendarEventDTO> response = calendarEventController.addAttendee(1L, 2L, userDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test Event", response.getBody().getTitle());

        verify(userService).getCurrentUser("test@example.com");
        verify(calendarEventService).addAttendee(1L, 2L, user);
    }

    @Test
    void testRemoveAttendee() {
        // Given
        when(userService.getCurrentUser("test@example.com")).thenReturn(user);
        when(calendarEventService.removeAttendee(1L, 2L, user)).thenReturn(eventDTO);

        // When
        ResponseEntity<CalendarEventDTO> response = calendarEventController.removeAttendee(1L, 2L, userDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test Event", response.getBody().getTitle());

        verify(userService).getCurrentUser("test@example.com");
        verify(calendarEventService).removeAttendee(1L, 2L, user);
    }
}
