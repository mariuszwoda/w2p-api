package pl.where2play.w2papi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.CalendarEventRepository;
import pl.where2play.w2papi.service.impl.GoogleCalendarServiceImpl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleCalendarServiceTest {

    @Mock
    private CalendarEventRepository eventRepository;

    @InjectMocks
    private GoogleCalendarServiceImpl googleCalendarService;

    private User user;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Set up test data
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .provider(User.AuthProvider.GOOGLE)
                .providerId("google123")
                .build();

        // Set private fields using reflection
        ReflectionTestUtils.setField(googleCalendarService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(googleCalendarService, "clientSecret", "test-client-secret");
    }

    @Test
    void testSynchronizeEvents() throws IOException {
        // Given
        CalendarEvent event1 = CalendarEvent.builder()
                .id(1L)
                .title("Event 1")
                .externalId("google123")
                .provider(CalendarEvent.CalendarProvider.GOOGLE)
                .owner(user)
                .build();

        CalendarEvent event2 = CalendarEvent.builder()
                .id(2L)
                .title("Event 2")
                .externalId("google456")
                .provider(CalendarEvent.CalendarProvider.GOOGLE)
                .owner(user)
                .build();

        // Create a spy of the service to mock the fetchEvents method
        GoogleCalendarServiceImpl serviceSpy = spy(googleCalendarService);
        doReturn(Arrays.asList(event1, event2)).when(serviceSpy).fetchEvents(user);

        when(eventRepository.findByOwner(user)).thenReturn(Collections.singletonList(event1));
        when(eventRepository.save(any(CalendarEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<CalendarEvent> result = serviceSpy.synchronizeEvents(user);

        // Then
        assertEquals(2, result.size());
        verify(eventRepository).findByOwner(user);
        verify(eventRepository, times(2)).save(any(CalendarEvent.class));
    }

    @Test
    void testGetAuthorizationUrl() {
        // When
        String url = googleCalendarService.getAuthorizationUrl();

        // Then
        assertNotNull(url);
        assertTrue(url.contains("accounts.google.com"));
        assertTrue(url.contains("oauth2/auth"));
        assertTrue(url.contains("test-client-id"));
    }

    @Test
    void testExchangeCodeForToken() {
        // When
        boolean result = googleCalendarService.exchangeCodeForToken("test-code", user);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsAuthorized() {
        // When
        boolean result = googleCalendarService.isAuthorized(user);

        // Then
        assertTrue(result);
    }
}