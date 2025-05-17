package pl.where2play.w2papi.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CalendarEventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User attendee;
    private CalendarEvent event1;
    private CalendarEvent event2;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Create users
        owner = User.builder()
                .email("owner@example.com")
                .name("Event Owner")
                .provider(User.AuthProvider.GOOGLE)
                .providerId("owner123")
                .build();

        attendee = User.builder()
                .email("attendee@example.com")
                .name("Event Attendee")
                .provider(User.AuthProvider.GOOGLE)
                .providerId("attendee123")
                .build();

        // Save users
        owner = entityManager.persist(owner);
        attendee = entityManager.persist(attendee);

        // Create events
        event1 = CalendarEvent.builder()
                .title("Test Event 1")
                .description("Description for test event 1")
                .startTime(now)
                .endTime(now.plusHours(1))
                .location("Test Location 1")
                .owner(owner)
                .provider(CalendarEvent.CalendarProvider.GOOGLE)
                .externalId("google123")
                .build();

        event2 = CalendarEvent.builder()
                .title("Test Event 2")
                .description("Description for test event 2")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(1).plusHours(2))
                .location("Test Location 2")
                .owner(owner)
                .provider(CalendarEvent.CalendarProvider.LOCAL)
                .build();

        // Add attendee to event2
        event2.getAttendees().add(attendee);

        // Save events
        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.flush();
    }

    @Test
    void testFindByOwner() {
        // When
        List<CalendarEvent> events = calendarEventRepository.findByOwner(owner);

        // Then
        assertEquals(2, events.size());
        assertTrue(events.contains(event1));
        assertTrue(events.contains(event2));
    }

    @Test
    void testFindByAttendeeAndDeletedFalse() {
        // When
        List<CalendarEvent> events = calendarEventRepository.findByAttendeeAndDeletedFalse(attendee);

        // Then
        assertEquals(1, events.size());
        assertTrue(events.contains(event2));
    }

    @Test
    void testFindAllEventsForUser() {
        // When
        List<CalendarEvent> ownerEvents = calendarEventRepository.findAllEventsForUser(owner);
        List<CalendarEvent> attendeeEvents = calendarEventRepository.findAllEventsForUser(attendee);

        // Then
        assertEquals(2, ownerEvents.size());
        assertTrue(ownerEvents.contains(event1));
        assertTrue(ownerEvents.contains(event2));

        assertEquals(1, attendeeEvents.size());
        assertTrue(attendeeEvents.contains(event2));
    }

    @Test
    void testFindAllEventsForUserInDateRange() {
        // When
        List<CalendarEvent> todayEvents = calendarEventRepository.findAllEventsForUserInDateRange(
                owner, now.minusHours(1), now.plusHours(2));

        List<CalendarEvent> tomorrowEvents = calendarEventRepository.findAllEventsForUserInDateRange(
                owner, now.plusDays(1).minusHours(1), now.plusDays(1).plusHours(3));

        List<CalendarEvent> allEvents = calendarEventRepository.findAllEventsForUserInDateRange(
                owner, now.minusHours(1), now.plusDays(2));

        // Then
        assertEquals(1, todayEvents.size());
        assertTrue(todayEvents.contains(event1));

        assertEquals(1, tomorrowEvents.size());
        assertTrue(tomorrowEvents.contains(event2));

        assertEquals(2, allEvents.size());
        assertTrue(allEvents.contains(event1));
        assertTrue(allEvents.contains(event2));
    }

    @Test
    void testFindByExternalIdAndProviderAndDeletedFalse() {
        // When
        Optional<CalendarEvent> foundEvent = calendarEventRepository.findByExternalIdAndProviderAndDeletedFalse(
                "google123", CalendarEvent.CalendarProvider.GOOGLE);

        Optional<CalendarEvent> notFoundEvent = calendarEventRepository.findByExternalIdAndProviderAndDeletedFalse(
                "nonexistent", CalendarEvent.CalendarProvider.GOOGLE);

        // Then
        assertTrue(foundEvent.isPresent());
        assertEquals(event1.getId(), foundEvent.get().getId());

        assertFalse(notFoundEvent.isPresent());
    }
}
