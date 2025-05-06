package pl.where2play.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.where2play.api.model.CalendarEvent;
import pl.where2play.api.service.CalendarEventService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CalendarEventControllerIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public CalendarEventService calendarEventService() {
            return Mockito.mock(CalendarEventService.class);
        }
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Autowired
    private CalendarEventService calendarEventService;

    @Test
    void getAllEvents_ShouldReturnAllEvents() throws Exception {
        // Arrange
        CalendarEvent event = new CalendarEvent();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setDescription("Test Description");
        event.setStartTime(LocalDateTime.now().plusHours(1));
        event.setEndTime(LocalDateTime.now().plusHours(2));

        when(calendarEventService.getAllEvents()).thenReturn(Arrays.asList(event));

        // Act & Assert
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Event")))
                .andExpect(jsonPath("$[0].description", is("Test Description")));
    }

    @Test
    void getEventById_WhenEventExists_ShouldReturnEvent() throws Exception {
        // Arrange
        CalendarEvent event = new CalendarEvent();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setDescription("Test Description");
        event.setStartTime(LocalDateTime.now().plusHours(1));
        event.setEndTime(LocalDateTime.now().plusHours(2));

        when(calendarEventService.getEventById(1L)).thenReturn(Optional.of(event));

        // Act & Assert
        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Event")))
                .andExpect(jsonPath("$.description", is("Test Description")));
    }

    @Test
    void getEventById_WhenEventDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(calendarEventService.getEventById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/events/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createEvent_ShouldCreateAndReturnEvent() throws Exception {
        // Arrange
        CalendarEvent eventToCreate = new CalendarEvent();
        eventToCreate.setTitle("New Event");
        eventToCreate.setDescription("New Description");
        eventToCreate.setStartTime(LocalDateTime.now().plusHours(1));
        eventToCreate.setEndTime(LocalDateTime.now().plusHours(2));

        CalendarEvent createdEvent = new CalendarEvent();
        createdEvent.setId(1L);
        createdEvent.setTitle("New Event");
        createdEvent.setDescription("New Description");
        createdEvent.setStartTime(eventToCreate.getStartTime());
        createdEvent.setEndTime(eventToCreate.getEndTime());

        when(calendarEventService.createEvent(any(CalendarEvent.class))).thenReturn(createdEvent);

        // Act & Assert
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("New Event")))
                .andExpect(jsonPath("$.description", is("New Description")));
    }

    @Test
    void updateEvent_WhenEventExists_ShouldUpdateAndReturnEvent() throws Exception {
        // Arrange
        CalendarEvent eventToUpdate = new CalendarEvent();
        eventToUpdate.setTitle("Updated Event");
        eventToUpdate.setDescription("Updated Description");
        eventToUpdate.setStartTime(LocalDateTime.now().plusHours(3));
        eventToUpdate.setEndTime(LocalDateTime.now().plusHours(4));

        CalendarEvent updatedEvent = new CalendarEvent();
        updatedEvent.setId(1L);
        updatedEvent.setTitle("Updated Event");
        updatedEvent.setDescription("Updated Description");
        updatedEvent.setStartTime(eventToUpdate.getStartTime());
        updatedEvent.setEndTime(eventToUpdate.getEndTime());

        when(calendarEventService.updateEvent(eq(1L), any(CalendarEvent.class))).thenReturn(updatedEvent);

        // Act & Assert
        mockMvc.perform(put("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Updated Event")))
                .andExpect(jsonPath("$.description", is("Updated Description")));
    }

    @Test
    void searchEventsByTitle_ShouldReturnMatchingEvents() throws Exception {
        // Arrange
        CalendarEvent event = new CalendarEvent();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setDescription("Test Description");

        when(calendarEventService.searchEventsByTitle("Test")).thenReturn(Arrays.asList(event));

        // Act & Assert
        mockMvc.perform(get("/api/events/search").param("title", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Event")));
    }
}
