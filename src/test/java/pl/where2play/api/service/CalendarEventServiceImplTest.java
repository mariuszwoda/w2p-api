package pl.where2play.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.where2play.api.model.CalendarEvent;
import pl.where2play.api.repository.CalendarEventRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarEventServiceImplTest {

    @Mock
    private CalendarEventRepository calendarEventRepository;

    @InjectMocks
    private CalendarEventServiceImpl calendarEventService;

    private CalendarEvent testEvent;
    private LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        testEvent = new CalendarEvent();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setStartTime(now.plusHours(1));
        testEvent.setEndTime(now.plusHours(2));
        testEvent.setLocation("Test Location");
        testEvent.setStatus(CalendarEvent.EventStatus.SCHEDULED);
        testEvent.setCreatedBy("test_user");
    }

    @Test
    void getAllEvents_ShouldReturnAllEvents() {
        // Arrange
        List<CalendarEvent> events = Arrays.asList(testEvent);
        when(calendarEventRepository.findAll()).thenReturn(events);

        // Act
        List<CalendarEvent> result = calendarEventService.getAllEvents();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Event", result.get(0).getTitle());
        verify(calendarEventRepository, times(1)).findAll();
    }

    @Test
    void getEventById_WhenEventExists_ShouldReturnEvent() {
        // Arrange
        when(calendarEventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act
        Optional<CalendarEvent> result = calendarEventService.getEventById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Event", result.get().getTitle());
        verify(calendarEventRepository, times(1)).findById(1L);
    }

    @Test
    void getEventById_WhenEventDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(calendarEventRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<CalendarEvent> result = calendarEventService.getEventById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(calendarEventRepository, times(1)).findById(999L);
    }

    @Test
    void createEvent_ShouldSaveAndReturnEvent() {
        // Arrange
        when(calendarEventRepository.save(any(CalendarEvent.class))).thenReturn(testEvent);

        // Act
        CalendarEvent result = calendarEventService.createEvent(testEvent);

        // Assert
        assertNotNull(result);
        assertEquals("Test Event", result.getTitle());
        verify(calendarEventRepository, times(1)).save(testEvent);
    }

    @Test
    void updateEvent_WhenEventExists_ShouldUpdateAndReturnEvent() {
        // Arrange
        CalendarEvent updatedEvent = new CalendarEvent();
        updatedEvent.setTitle("Updated Title");
        updatedEvent.setDescription("Updated Description");
        updatedEvent.setStartTime(now.plusHours(3));
        updatedEvent.setEndTime(now.plusHours(4));
        updatedEvent.setLocation("Updated Location");
        updatedEvent.setStatus(CalendarEvent.EventStatus.COMPLETED);

        when(calendarEventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(calendarEventRepository.save(any(CalendarEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CalendarEvent result = calendarEventService.updateEvent(1L, updatedEvent);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(now.plusHours(3), result.getStartTime());
        assertEquals(now.plusHours(4), result.getEndTime());
        assertEquals("Updated Location", result.getLocation());
        assertEquals(CalendarEvent.EventStatus.COMPLETED, result.getStatus());
        verify(calendarEventRepository, times(1)).findById(1L);
        verify(calendarEventRepository, times(1)).save(any(CalendarEvent.class));
    }

    @Test
    void searchEventsByTitle_ShouldReturnMatchingEvents() {
        // Arrange
        List<CalendarEvent> events = Arrays.asList(testEvent);
        when(calendarEventRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(events);

        // Act
        List<CalendarEvent> result = calendarEventService.searchEventsByTitle("Test");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Event", result.get(0).getTitle());
        verify(calendarEventRepository, times(1)).findByTitleContainingIgnoreCase("Test");
    }

    @Test
    void getEventsBetweenDates_ShouldReturnEventsInRange() {
        // Arrange
        List<CalendarEvent> events = Arrays.asList(testEvent);
        LocalDateTime start = now;
        LocalDateTime end = now.plusHours(3);
        when(calendarEventRepository.findByStartTimeBetween(start, end)).thenReturn(events);

        // Act
        List<CalendarEvent> result = calendarEventService.getEventsBetweenDates(start, end);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Event", result.get(0).getTitle());
        verify(calendarEventRepository, times(1)).findByStartTimeBetween(start, end);
    }
}