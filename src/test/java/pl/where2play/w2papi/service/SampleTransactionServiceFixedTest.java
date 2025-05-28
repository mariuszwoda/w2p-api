package pl.where2play.w2papi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.UserRepository;
import pl.where2play.w2papi.service.impl.GoogleCalendarServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SampleTransactionServiceFixedTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GoogleCalendarServiceImpl googleCalendarService;

    @InjectMocks
    private SampleTransactionServiceFixed sampleTransactionService;

    @Mock
    private SampleTransactionServiceFixed self;

    private User testUser;
    private CalendarEvent testEvent;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("mail@test.com")
                .build();

        testEvent = new CalendarEvent();

        // Set up the self reference
        sampleTransactionService.setSelf(self);
    }

    @Test
    void testPerformTransaction() {
        // Given
        doReturn(testUser).when(self).saveUserData(any(User.class));

        // When
        User result = sampleTransactionService.performTransaction();

        // Then
        assertNotNull(result);
        assertEquals("mail@test.com", result.getEmail());

        // Verify that saveUserData was called with a user object
        verify(self).saveUserData(any(User.class));
    }

    @Test
    void testSaveUserData() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = sampleTransactionService.saveUserData(testUser);

        // Then
        assertNotNull(result);
        assertEquals("mail@test.com", result.getEmail());

        // Verify that userRepository.save was called
        verify(userRepository).save(testUser);
    }

    @Test
    void testSaveCalendarEvent() {
        // Given
        when(googleCalendarService.createEvent(any(CalendarEvent.class), any(User.class))).thenReturn(testEvent);

        // When
        sampleTransactionService.saveCalendarEvent(testUser);

        // Then
        // Verify that googleCalendarService.createEvent was called with a CalendarEvent and User
        verify(googleCalendarService).createEvent(any(CalendarEvent.class), eq(testUser));
    }

    @Test
    void testTransactionalBehavior() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(googleCalendarService.createEvent(any(CalendarEvent.class), any(User.class)))
            .thenThrow(new RuntimeException("Test exception"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            sampleTransactionService.saveUserData(testUser);
        });

        // Verify that userRepository.save was called but the transaction should be rolled back
        verify(userRepository).save(testUser);
    }
}
