package pl.where2play.w2papi.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import pl.where2play.w2papi.dto.request.AuthRequest;
import pl.where2play.w2papi.dto.request.CreateCalendarEventRequest;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.UserRepository;
import pl.where2play.w2papi.security.JwtTokenProvider;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev", "test"})
class CalendarEventE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String authToken;
    private User testUser;
    private Long eventId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        //RestAssured.useRelaxedHTTPSValidation();
        //RestAssured.authentication = RestAssured.basic("admin", "admin");

        now = LocalDateTime.now();

        // Create test user if it doesn't exist
        String testEmail = "e2e-test@example.com";
        testUser = userRepository.findByEmail(testEmail)
                .orElseGet(() -> {
                    User user = User.builder()
                            .email(testEmail)
                            .name("E2E Test User")
                            .provider(User.AuthProvider.GOOGLE)
                            .providerId("e2e123")
                            .build();
                    return userRepository.save(user);
                });

        // Generate JWT token for the test user
        authToken = jwtTokenProvider.generateToken(testUser.getEmail());
    }

    @Test
    void testFullEventLifecycle() {
        // 1. Create a new event
        CreateCalendarEventRequest createRequest = new CreateCalendarEventRequest();
        createRequest.setTitle("E2E Test Event");
        createRequest.setDescription("E2E test description");
        createRequest.setStartTime(now.plusHours(1));
        createRequest.setEndTime(now.plusHours(2));
        createRequest.setLocation("E2E Test Location");
        createRequest.setCalendarProvider("LOCAL");

        Response createResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post("/events")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("title", equalTo("E2E Test Event"))
                .body("description", equalTo("E2E test description"))
                .body("location", equalTo("E2E Test Location"))
                .body("owner.email", equalTo(testUser.getEmail()))
                .extract()
                .response();

        // Extract event ID for later use
        eventId = Long.valueOf(createResponse.path("id").toString());

        // 2. Get the created event
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/events/{id}", eventId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(eventId.intValue()))
                .body("title", equalTo("E2E Test Event"))
                .body("description", equalTo("E2E test description"))
                .body("location", equalTo("E2E Test Location"));

        // 3. Get all events
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/events")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("find { it.id == " + eventId + " }.title", equalTo("E2E Test Event"));

        // 4. Update the event
        Map<String, String> updateRequest = new HashMap<>();
        updateRequest.put("title", "Updated E2E Event");
        updateRequest.put("description", "Updated E2E description");

        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/events/{id}", eventId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(eventId.intValue()))
                .body("title", equalTo("Updated E2E Event"))
                .body("description", equalTo("Updated E2E description"));

        // 5. Verify the update
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/events/{id}", eventId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("title", equalTo("Updated E2E Event"))
                .body("description", equalTo("Updated E2E description"));

        // 6. Get events in date range
        given()
                .header("Authorization", "Bearer " + authToken)
                .param("start", now.toString())
                .param("end", now.plusHours(3).toString())
                .when()
                .get("/events/range")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("find { it.id == " + eventId + " }.title", equalTo("Updated E2E Event"));

        // 7. Hard delete the event (for E2E tests)
        given()
                .header("Authorization", "Bearer " + authToken)
                .param("isE2ETest", true)
                .when()
                .delete("/events/{id}/hard", eventId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // 8. Verify deletion
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/events/{id}", eventId)
                .then()
//                .statusCode(HttpStatus.BAD_REQUEST.value()); // or NOT_FOUND, depending on your implementation
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void testAuthenticationEndpoint() {
        // Create auth request
        AuthRequest authRequest = new AuthRequest();
        authRequest.setToken("test-token");
        authRequest.setProvider("GOOGLE");

        // Test authentication endpoint
        given()
                .contentType(ContentType.JSON)
                .body(authRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("token", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .body("expiresIn", greaterThan(0))
                .body("user", notNullValue());
    }

    @Test
    void testUnauthorizedAccess() {
        // Try to access protected endpoint without authentication
        given()
                .when()
                .get("/events")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
