package pl.where2play.api.controller;

import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import pl.where2play.api.model.CalendarEvent;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CalendarEventRestAssuredTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
//        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);

//
//        "org.apache.http.wire"
//        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.basePath = "/api/events";
    }

    @Test
    void getAllEvents_ShouldReturnEvents() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get()
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("$", not(empty()));
    }

    @Test
    void getEventById_WhenEventExists_ShouldReturnEvent() {
        // First, get all events to find an existing ID
        Integer eventId = given()
            .contentType(ContentType.JSON)
        .when()
            .get()
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .path("[0].id");

        // Then get the specific event
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", eventId)
        .when()
            .get("/{id}")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(eventId))
            .body("title", not(emptyOrNullString()))
            .body("startTime", not(nullValue()))
            .body("endTime", not(nullValue()));
    }

    @Test
    void getEventById_WhenEventDoesNotExist_ShouldReturnNotFound() {
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", 999999)
        .when()
            .get("/{id}")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void createEvent_ShouldCreateAndReturnEvent() {
        // Create a new event
        CalendarEvent newEvent = new CalendarEvent();
        newEvent.setTitle("REST Assured Test Event");
        newEvent.setDescription("Created by REST Assured test");
        newEvent.setStartTime(LocalDateTime.now().plusDays(1));
        newEvent.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        newEvent.setLocation("Test Location");
        newEvent.setStatus(CalendarEvent.EventStatus.SCHEDULED);
        newEvent.setCreatedBy("rest-assured-test");

        given()
            .contentType(ContentType.JSON)
            .body(newEvent)
        .when()
            .post()
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("id", not(nullValue()))
            .body("title", equalTo("REST Assured Test Event"))
            .body("description", equalTo("Created by REST Assured test"))
            .body("location", equalTo("Test Location"))
            .body("status", equalTo("SCHEDULED"))
            .body("createdBy", equalTo("rest-assured-test"));
    }

    @Test
    void updateEvent_WhenEventExists_ShouldUpdateAndReturnEvent() {
        // First, create a new event
        CalendarEvent newEvent = new CalendarEvent();
        newEvent.setTitle("Event to Update");
        newEvent.setDescription("This event will be updated");
        newEvent.setStartTime(LocalDateTime.now().plusDays(2));
        newEvent.setEndTime(LocalDateTime.now().plusDays(2).plusHours(1));
        newEvent.setLocation("Original Location");
        newEvent.setStatus(CalendarEvent.EventStatus.SCHEDULED);
        newEvent.setCreatedBy("rest-assured-test");

        Integer eventId = given()
            .contentType(ContentType.JSON)
            .body(newEvent)
        .when()
            .post()
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("id");

        // Update the event
        newEvent.setTitle("Updated Event Title");
        newEvent.setDescription("This event has been updated");
        newEvent.setLocation("New Location");
        newEvent.setStatus(CalendarEvent.EventStatus.COMPLETED);

        given()
            .contentType(ContentType.JSON)
            .pathParam("id", eventId)
            .body(newEvent)
        .when()
            .put("/{id}")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(eventId))
            .body("title", equalTo("Updated Event Title"))
            .body("description", equalTo("This event has been updated"))
            .body("location", equalTo("New Location"))
            .body("status", equalTo("COMPLETED"));
    }

    //@Disabled
    @Test
    void deleteEvent_WhenEventExists_ShouldDeleteEvent() {
        // First, create a new event
        CalendarEvent newEvent = new CalendarEvent();
        newEvent.setTitle("Event to Delete");
        newEvent.setDescription("This event will be deleted");
        newEvent.setStartTime(LocalDateTime.now().plusDays(3));
        newEvent.setEndTime(LocalDateTime.now().plusDays(3).plusHours(1));
        newEvent.setLocation("Delete Test Location");
        newEvent.setStatus(CalendarEvent.EventStatus.SCHEDULED);
        newEvent.setCreatedBy("rest-assured-test");

        Integer eventId = given()
            .contentType(ContentType.JSON)
            .body(newEvent)
        .when()
            .post()
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("id");

        // Delete the event
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", eventId)
        .when()
            .delete("/{id}")
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // Verify the event is deleted
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", eventId)
        .when()
            .get("/{id}")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void searchEventsByTitle_ShouldReturnMatchingEvents() {
        // First, create an event with a unique title
        String uniqueTitle = "UniqueSearchTitle" + System.currentTimeMillis();
        CalendarEvent newEvent = new CalendarEvent();
        newEvent.setTitle(uniqueTitle);
        newEvent.setDescription("Event for search test");
        newEvent.setStartTime(LocalDateTime.now().plusDays(4));
        newEvent.setEndTime(LocalDateTime.now().plusDays(4).plusHours(1));
        newEvent.setLocation("Search Test Location");
        newEvent.setStatus(CalendarEvent.EventStatus.SCHEDULED);
        newEvent.setCreatedBy("rest-assured-test");

        given()
            .contentType(ContentType.JSON)
            .body(newEvent)
        .when()
            .post()
        .then()
            .statusCode(HttpStatus.CREATED.value());

        // Search for the event by title
        given()
            .contentType(ContentType.JSON)
            .queryParam("title", uniqueTitle)
        .when()
            .get("/search")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].title", containsString(uniqueTitle));
    }
}