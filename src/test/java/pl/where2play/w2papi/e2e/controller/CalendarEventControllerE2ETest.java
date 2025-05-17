package pl.where2play.w2papi.e2e.controller;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import pl.where2play.w2papi.dto.request.CreateCalendarEventRequest;
import pl.where2play.w2papi.dto.request.UpdateCalendarEventRequest;
import pl.where2play.w2papi.e2e.framework.BaseApiTest;
import pl.where2play.w2papi.e2e.framework.RequestConfig;
import pl.where2play.w2papi.e2e.framework.constants.ApiEndpoints;
import pl.where2play.w2papi.e2e.framework.util.JsonUtils;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * E2E tests for the Calendar Event Controller.
 */
public class CalendarEventControllerE2ETest extends BaseApiTest {

    @Autowired
    private JsonUtils jsonUtils;

    @Test
    @DisplayName("Test full event lifecycle")
    void testFullEventLifecycle() {
        // 1. Create a new event
        CreateCalendarEventRequest createRequest = testDataService.createTestCalendarEventRequest();

        Response createResponse = post(
                ApiEndpoints.CalendarEvent.BASE,
                RequestConfig.withBody(createRequest)
        );

        createResponse.then()
                .statusCode(HttpStatus.OK.value())
                .body("title", equalTo(createRequest.getTitle()))
                .body("description", equalTo(createRequest.getDescription()))
                .body("location", equalTo(createRequest.getLocation()))
                .body("owner.email", equalTo(testUser.getEmail()));

        // Extract event ID for later use
        Long eventId = Long.valueOf(createResponse.path("id").toString());

        // 2. Get the created event
        Response getResponse = get(
                ApiEndpoints.CalendarEvent.GET_EVENT,
                RequestConfig.withPathParams(Map.of("id", eventId))
        );

        getResponse.then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(eventId.intValue()))
                .body("title", equalTo(createRequest.getTitle()))
                .body("description", equalTo(createRequest.getDescription()))
                .body("location", equalTo(createRequest.getLocation()));

        // 3. Get all events
        Response getAllResponse = get(
                ApiEndpoints.CalendarEvent.BASE,
                RequestConfig.empty()
        );

        getAllResponse.then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("find { it.id == " + eventId + " }.title", equalTo(createRequest.getTitle()));

        // 4. Update the event
        UpdateCalendarEventRequest updateRequest = testDataService.createTestCalendarEventUpdateRequest();

        Response updateResponse = put(
                ApiEndpoints.CalendarEvent.UPDATE_EVENT,
                RequestConfig.builder()
                        .pathParams(Map.of("id", eventId))
                        .body(updateRequest)
                        .build()
        );

        updateResponse.then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(eventId.intValue()))
                .body("title", equalTo(updateRequest.getTitle()))
                .body("description", equalTo(updateRequest.getDescription()));

        // 5. Verify the update
        Response verifyUpdateResponse = get(
                ApiEndpoints.CalendarEvent.GET_EVENT,
                RequestConfig.withPathParams(Map.of("id", eventId))
        );

        verifyUpdateResponse.then()
                .statusCode(HttpStatus.OK.value())
                .body("title", equalTo(updateRequest.getTitle()))
                .body("description", equalTo(updateRequest.getDescription()));

        // 6. Get events in date range
        LocalDateTime now = LocalDateTime.now();

        Response dateRangeResponse = get(
                ApiEndpoints.CalendarEvent.GET_EVENTS_IN_RANGE,
                RequestConfig.withQueryParams(Map.of(
                        "start", now.minusDays(1).toString(),
                        "end", now.plusDays(1).toString()
                ))
        );

        dateRangeResponse.then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("find { it.id == " + eventId + " }.title", equalTo(updateRequest.getTitle()));

        // 7. Hard delete the event (for E2E tests)
        Response deleteResponse = delete(
                ApiEndpoints.CalendarEvent.HARD_DELETE_EVENT,
                RequestConfig.builder()
                        .pathParams(Map.of("id", eventId))
                        .queryParams(Map.of("isE2ETest", true))
                        .build()
        );

        deleteResponse.then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // 8. Verify deletion
        Response verifyDeletionResponse = get(
                ApiEndpoints.CalendarEvent.GET_EVENT,
                RequestConfig.withPathParams(Map.of("id", eventId))
        );

        verifyDeletionResponse.then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Test creating events with LOCAL provider")
    void testCreateEventWithLocalProvider() {
        // Create a new event - exactly like in testFullEventLifecycle
        CreateCalendarEventRequest createRequest = testDataService.createTestCalendarEventRequest();

        Response createResponse = post(
                ApiEndpoints.CalendarEvent.BASE,
                RequestConfig.withBody(createRequest)
        );

        createResponse.then()
                .statusCode(HttpStatus.OK.value())
                .body("title", equalTo(createRequest.getTitle()))
                .body("description", equalTo(createRequest.getDescription()))
                .body("location", equalTo(createRequest.getLocation()))
                .body("owner.email", equalTo(testUser.getEmail()));

        // Extract event ID for later use
        Long eventId = Long.valueOf(createResponse.path("id").toString());

        // Clean up - delete the event
        Response deleteResponse = delete(
                ApiEndpoints.CalendarEvent.HARD_DELETE_EVENT,
                RequestConfig.builder()
                        .pathParams(Map.of("id", eventId))
                        .queryParams(Map.of("isE2ETest", true))
                        .build()
        );

        deleteResponse.then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("Test adding and removing attendees")
    void testAddingAndRemovingAttendees() {
        // 1. Create a new event - exactly like in testFullEventLifecycle
        CreateCalendarEventRequest createRequest = testDataService.createTestCalendarEventRequest();

        Response createResponse = post(
                ApiEndpoints.CalendarEvent.BASE,
                RequestConfig.withBody(createRequest)
        );

        createResponse.then()
                .statusCode(HttpStatus.OK.value())
                .body("title", equalTo(createRequest.getTitle()))
                .body("description", equalTo(createRequest.getDescription()))
                .body("location", equalTo(createRequest.getLocation()))
                .body("owner.email", equalTo(testUser.getEmail()));

        // Extract event ID for later use
        Long eventId = Long.valueOf(createResponse.path("id").toString());

        // 2. Create another user to be an attendee
        User attendee = testDataService.createTestUser();

        // 3. Add attendee to the event
        Response addAttendeeResponse = post(
                ApiEndpoints.CalendarEvent.ADD_ATTENDEE,
                RequestConfig.withPathParams(Map.of(
                        "eventId", eventId,
                        "userId", attendee.getId()
                ))
        );

        addAttendeeResponse.then()
                .statusCode(HttpStatus.OK.value());

        // 4. Remove attendee from the event
        Response removeAttendeeResponse = delete(
                ApiEndpoints.CalendarEvent.REMOVE_ATTENDEE,
                RequestConfig.withPathParams(Map.of(
                        "eventId", eventId,
                        "userId", attendee.getId()
                ))
        );

        removeAttendeeResponse.then()
                .statusCode(HttpStatus.OK.value());

        // 5. Clean up - delete the event
        Response deleteResponse = delete(
                ApiEndpoints.CalendarEvent.HARD_DELETE_EVENT,
                RequestConfig.builder()
                        .pathParams(Map.of("id", eventId))
                        .queryParams(Map.of("isE2ETest", true))
                        .build()
        );

        deleteResponse.then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("Test unauthorized access")
    void testUnauthorizedAccess() {
        // Try to access protected endpoint without authentication
        Response response = requestWithoutAuth(
                ApiEndpoints.CalendarEvent.BASE,
                "GET",
                RequestConfig.empty()
        );

        response.then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Test simple event creation")
    void testSimpleEventCreation() {
        // Use testDataService to create the request
        CreateCalendarEventRequest createRequest = testDataService.createTestCalendarEventRequest();
        createRequest.setTitle("Simple Test Event");
        createRequest.setDescription("Simple test description");

        // Create the event
        Response response = post(
                ApiEndpoints.CalendarEvent.BASE,
                RequestConfig.withBody(createRequest)
        );

        // Verify response
        Long eventId = response.then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("id");

        // Clean up
        delete(
                ApiEndpoints.CalendarEvent.HARD_DELETE_EVENT,
                RequestConfig.builder()
                        .pathParams(Map.of("id", eventId))
                        .queryParams(Map.of("isE2ETest", true))
                        .build()
        );
    }
}
