package pl.where2play.w2papi.e2e.controller;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import pl.where2play.w2papi.dto.request.AuthRequest;
import pl.where2play.w2papi.e2e.framework.BaseApiTest;
import pl.where2play.w2papi.e2e.framework.RequestConfig;
import pl.where2play.w2papi.e2e.framework.constants.ApiEndpoints;

import static org.hamcrest.Matchers.*;

/**
 * E2E tests for the Auth Controller.
 */
public class AuthControllerE2ETest extends BaseApiTest {

    @Test
    @DisplayName("Test successful authentication")
    void testSuccessfulAuthentication() {
        // Create auth request
        AuthRequest authRequest = testDataService.createTestAuthRequest();

        // Test authentication endpoint
        Response response = post(
                ApiEndpoints.Auth.LOGIN,
                RequestConfig.withBody(authRequest)
        );

        // Verify response
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("token", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .body("expiresIn", greaterThan(0))
                .body("user", notNullValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"GOOGLE", "FACEBOOK"})
    @DisplayName("Test authentication with different providers")
    void testAuthenticationWithDifferentProviders(String provider) {
        // Create auth request with specific provider
        AuthRequest authRequest = AuthRequest.builder()
                .token("test-token")
                .provider(provider)
                .build();

        // Test authentication endpoint
        Response response = post(
                ApiEndpoints.Auth.LOGIN,
                RequestConfig.withBody(authRequest)
        );

        // Verify response
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("token", notNullValue())
                .body("user.provider", equalTo(provider));
    }

    @Test
    @DisplayName("Test authentication with invalid request")
    void testAuthenticationWithInvalidRequest() {
        // Create invalid auth request (missing required fields)
        AuthRequest invalidRequest = AuthRequest.builder().build();

        // Test authentication endpoint
        Response response = post(
                ApiEndpoints.Auth.LOGIN,
                RequestConfig.withBody(invalidRequest)
        );

        // Verify response
        response.then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Test authentication with invalid provider")
    void testAuthenticationWithInvalidProvider() {
        // Create auth request with invalid provider
        AuthRequest authRequest = AuthRequest.builder()
                .token("test-token")
                .provider("INVALID_PROVIDER")
                .build();

        // Test authentication endpoint
        Response response = post(
                ApiEndpoints.Auth.LOGIN,
                RequestConfig.withBody(authRequest)
        );

        // Verify response
        response.then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}