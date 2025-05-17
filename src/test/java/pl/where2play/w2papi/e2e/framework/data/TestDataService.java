package pl.where2play.w2papi.e2e.framework.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import pl.where2play.w2papi.dto.request.AuthRequest;
import pl.where2play.w2papi.dto.request.CreateCalendarEventRequest;
import pl.where2play.w2papi.dto.request.UpdateCalendarEventRequest;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.CalendarEventRepository;
import pl.where2play.w2papi.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

/**
 * Service for generating test data for E2E tests.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TestDataService {

    private final UserRepository userRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final ObjectMapper objectMapper;

    private static final String TEST_EMAIL_PREFIX = "e2e-test-";
    private static final String TEST_EMAIL_DOMAIN = "@example.com";

    /**
     * Creates a test user for E2E tests.
     * If a user with the same email already exists, returns that user.
     *
     * @return the test user
     */
    public User createTestUser() {
        String testEmail = TEST_EMAIL_PREFIX + UUID.randomUUID() + TEST_EMAIL_DOMAIN;
        return userRepository.findByEmail(testEmail)
                .orElseGet(() -> {
                    User user = User.builder()
                            .email(testEmail)
                            .name("E2E Test User")
                            .provider(User.AuthProvider.GOOGLE)
                            .providerId("e2e-" + UUID.randomUUID())
                            .build();
                    return userRepository.save(user);
                });
    }

    /**
     * Creates a test calendar event for E2E tests.
     *
     * @param owner the event owner
     * @return the test calendar event
     */
    public CalendarEvent createTestCalendarEvent(User owner) {
        LocalDateTime now = LocalDateTime.now();
        CalendarEvent event = CalendarEvent.builder()
                .title("E2E Test Event")
                .description("E2E test description")
                .startTime(now.plusHours(1))
                .endTime(now.plusHours(2))
                .location("E2E Test Location")
                .owner(owner)
                .attendees(new HashSet<>())
                .provider(CalendarEvent.CalendarProvider.LOCAL)
                .allDay(false)
                .deleted(false)
                .build();
        return calendarEventRepository.save(event);
    }

    /**
     * Creates a test auth request for E2E tests.
     *
     * @return the test auth request
     */
    public AuthRequest createTestAuthRequest() {
        return AuthRequest.builder()
                .token("test-token-" + UUID.randomUUID())
                .provider("GOOGLE")
                .build();
    }

    /**
     * Creates a test calendar event request for E2E tests.
     *
     * @return the test calendar event request
     */
    public CreateCalendarEventRequest createTestCalendarEventRequest() {
        LocalDateTime now = LocalDateTime.now();
        return CreateCalendarEventRequest.builder()
                .title("E2E Test Event")
                .description("E2E test description")
                .startTime(now.plusHours(1))
                .endTime(now.plusHours(2))
                .location("E2E Test Location")
                .calendarProvider("LOCAL")
                .allDay(false)
                .build();
    }

    /**
     * Creates a test calendar event update request for E2E tests.
     *
     * @return the test calendar event update request
     */
    public UpdateCalendarEventRequest createTestCalendarEventUpdateRequest() {
        LocalDateTime now = LocalDateTime.now();
        return UpdateCalendarEventRequest.builder()
                .title("Updated E2E Test Event")
                .description("Updated E2E test description")
                .startTime(now.plusHours(3))
                .endTime(now.plusHours(4))
                .location("Updated E2E Test Location")
                .build();
    }

    /**
     * Loads test data from a JSON file.
     *
     * @param resourcePath the path to the JSON file
     * @param valueType the type of the value to deserialize
     * @param <T> the type of the value
     * @return the deserialized value
     */
    public <T> T loadTestDataFromJson(String resourcePath, Class<T> valueType) {
        try {
            Resource resource = new ClassPathResource(resourcePath);
            try (InputStream inputStream = resource.getInputStream()) {
                return objectMapper.readValue(inputStream, valueType);
            }
        } catch (IOException e) {
            log.error("Failed to load test data from JSON file: {}", resourcePath, e);
            throw new RuntimeException("Failed to load test data from JSON file: " + resourcePath, e);
        }
    }
}