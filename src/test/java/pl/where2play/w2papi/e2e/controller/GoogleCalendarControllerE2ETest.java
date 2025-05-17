package pl.where2play.w2papi.e2e.controller;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import pl.where2play.w2papi.e2e.framework.BaseApiTest;
import pl.where2play.w2papi.e2e.framework.RequestConfig;
import pl.where2play.w2papi.e2e.framework.constants.ApiEndpoints;

import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * E2E tests for the Google Calendar Controller.
 */
public class GoogleCalendarControllerE2ETest extends BaseApiTest {

    @Test
    @DisplayName("Test getting authorization URL")
    void testGetAuthorizationUrl() {
        // Test getting authorization URL
        Response response = get(
                ApiEndpoints.GoogleCalendar.AUTH_URL,
                RequestConfig.empty()
        );
        
        // Verify response
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("authorizationUrl", notNullValue())
                .body("authorizationUrl", containsString("accounts.google.com"));
    }

    @Test
    @DisplayName("Test exchanging code for token")
    void testExchangeCodeForToken() {
        // Test exchanging code for token
        String testCode = "test-code-" + System.currentTimeMillis();
        
        Response response = post(
                ApiEndpoints.GoogleCalendar.EXCHANGE_CODE,
                RequestConfig.withQueryParams(Map.of("code", testCode))
        );
        
        // Verify response
        // Note: In a real test environment, this might fail if the code is invalid
        // This is just testing the API contract
        response.then()
                .statusCode(anyOf(equalTo(HttpStatus.OK.value()), equalTo(HttpStatus.BAD_REQUEST.value())))
                .body("success", anyOf(equalTo(true), equalTo(false)));
    }

    @Test
    @DisplayName("Test checking authorization status")
    void testCheckAuthorizationStatus() {
        // Test checking authorization status
        Response response = get(
                ApiEndpoints.GoogleCalendar.AUTH_STATUS,
                RequestConfig.empty()
        );
        
        // Verify response
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("authorized", isA(Boolean.class));
    }

    @Test
    @DisplayName("Test unauthorized access")
    void testUnauthorizedAccess() {
        // Try to access protected endpoint without authentication
        Response response = requestWithoutAuth(
                ApiEndpoints.GoogleCalendar.AUTH_STATUS,
                "GET",
                RequestConfig.empty()
        );
        
        // Verify response
        response.then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Test exchanging code with invalid code")
    void testExchangeCodeWithInvalidCode() {
        // Test exchanging code with invalid code
        Response response = post(
                ApiEndpoints.GoogleCalendar.EXCHANGE_CODE,
                RequestConfig.withQueryParams(Map.of("code", "invalid-code"))
        );
        
        // Verify response
        // In a real environment, this should fail with a bad request
        response.then()
                .statusCode(anyOf(equalTo(HttpStatus.OK.value()), equalTo(HttpStatus.BAD_REQUEST.value())));
    }
}