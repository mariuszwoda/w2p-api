package pl.where2play.w2papi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.where2play.w2papi.dto.request.CreateCalendarEventRequest;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.CalendarEventRepository;
import pl.where2play.w2papi.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
@Transactional
class CalendarEventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarEventRepository eventRepository;

    private User testUser;
    private CalendarEvent testEvent;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Create test user
        testUser = User.builder()
                .email("integration-test@example.com")
                .name("Integration Test User")
                .provider(User.AuthProvider.GOOGLE)
                .providerId("integration123")
                .build();
        testUser = userRepository.save(testUser);

        // Create test event
        testEvent = CalendarEvent.builder()
                .title("Integration Test Event")
                .description("Integration test description")
                .startTime(now)
                .endTime(now.plusHours(1))
                .location("Integration Test Location")
                .owner(testUser)
                .attendees(new HashSet<>())
                .provider(CalendarEvent.CalendarProvider.LOCAL)
                .build();
        testEvent = eventRepository.save(testEvent);
    }

    @Test
    @WithMockUser(username = "integration-test@example.com")
    void testGetAllEvents() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testEvent.getId()))
                .andExpect(jsonPath("$[0].title").value("Integration Test Event"))
                .andExpect(jsonPath("$[0].description").value("Integration test description"))
                .andExpect(jsonPath("$[0].location").value("Integration Test Location"))
                .andExpect(jsonPath("$[0].owner.id").value(testUser.getId()))
                .andExpect(jsonPath("$[0].owner.email").value("integration-test@example.com"));
    }

    @Test
    @WithMockUser(username = "integration-test@example.com")
    void testGetEvent() throws Exception {
        mockMvc.perform(get("/api/events/{id}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testEvent.getId()))
                .andExpect(jsonPath("$.title").value("Integration Test Event"))
                .andExpect(jsonPath("$.description").value("Integration test description"))
                .andExpect(jsonPath("$.location").value("Integration Test Location"))
                .andExpect(jsonPath("$.owner.id").value(testUser.getId()))
                .andExpect(jsonPath("$.owner.email").value("integration-test@example.com"));
    }

    @Test
    @WithMockUser(username = "integration-test@example.com")
    void testCreateEvent() throws Exception {
        // Create request
        CreateCalendarEventRequest request = CreateCalendarEventRequest.builder()
                .title("New Integration Event")
                .description("New integration description")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(1).plusHours(2))
                .location("New Integration Location")
                .calendarProvider("LOCAL")
                .build();

        // Perform request
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("New Integration Event"))
                .andExpect(jsonPath("$.description").value("New integration description"))
                .andExpect(jsonPath("$.location").value("New Integration Location"))
                .andExpect(jsonPath("$.owner.email").value("integration-test@example.com"));

        // Verify event count
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "integration-test@example.com")
    void testUpdateEvent() throws Exception {
        // Create update request
        String updatedTitle = "Updated Integration Event";
        String updatedDescription = "Updated integration description";

        String requestBody = String.format(
                "{\"title\":\"%s\",\"description\":\"%s\"}",
                updatedTitle, updatedDescription);

        // Perform update
        mockMvc.perform(put("/api/events/{id}", testEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testEvent.getId()))
                .andExpect(jsonPath("$.title").value(updatedTitle))
                .andExpect(jsonPath("$.description").value(updatedDescription));

        // Verify update
        mockMvc.perform(get("/api/events/{id}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(updatedTitle))
                .andExpect(jsonPath("$.description").value(updatedDescription));
    }

    @Test
    @WithMockUser(username = "integration-test@example.com")
    void testDeleteEvent() throws Exception {
        // Delete event
        mockMvc.perform(delete("/api/events/{id}", testEvent.getId())
                        .param("isE2ETest", "true"))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "integration-test@example.com")
    void testGetEventsInDateRange() throws Exception {
        // Create another event for tomorrow
        CalendarEvent tomorrowEvent = CalendarEvent.builder()
                .title("Tomorrow Event")
                .description("Tomorrow description")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(1).plusHours(1))
                .location("Tomorrow Location")
                .owner(testUser)
                .attendees(new HashSet<>())
                .provider(CalendarEvent.CalendarProvider.LOCAL)
                .build();
        eventRepository.save(tomorrowEvent);

        // Get today's events
        mockMvc.perform(get("/api/events/range")
                        .param("start", now.minusHours(1).toString())
                        .param("end", now.plusHours(2).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testEvent.getId()));

        // Get tomorrow's events
        mockMvc.perform(get("/api/events/range")
                        .param("start", now.plusDays(1).minusHours(1).toString())
                        .param("end", now.plusDays(1).plusHours(2).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Tomorrow Event"));

        // Get all events
        mockMvc.perform(get("/api/events/range")
                        .param("start", now.minusHours(1).toString())
                        .param("end", now.plusDays(2).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
