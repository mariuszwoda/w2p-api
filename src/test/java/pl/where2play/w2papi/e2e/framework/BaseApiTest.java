package pl.where2play.w2papi.e2e.framework;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import pl.where2play.w2papi.e2e.framework.data.TestDataService;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.UserRepository;
import pl.where2play.w2papi.security.JwtTokenProvider;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Base class for all API E2E tests.
 * Provides common functionality for setting up and executing API tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev", "test"})
public abstract class BaseApiTest {

    @LocalServerPort
    protected int port;

    @Value("${api.base-path:/api}")
    protected String basePath;

    @Value("${api.remote-url:}")
    protected String remoteUrl;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    @Autowired
    protected TestDataService testDataService;

    protected String authToken;
    protected User testUser;

    @BeforeEach
    void setUp() {
        // Configure RestAssured
        if (remoteUrl != null && !remoteUrl.isEmpty()) {
            // For testing remote applications
            RestAssured.baseURI = remoteUrl;
        } else {
            // For testing local applications
            RestAssured.port = port;
        }

        RestAssured.basePath = basePath;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Create test user and generate JWT token
        testUser = testDataService.createTestUser();
        authToken = jwtTokenProvider.generateToken(testUser.getEmail());
    }

    /**
     * Performs a GET request with authentication.
     *
     * @param endpoint the API endpoint
     * @param requestConfig the request configuration
     * @return the response
     */
    protected Response get(String endpoint, RequestConfig requestConfig) {
        return given()
                .header("Authorization", "Bearer " + authToken)
                .queryParams(requestConfig.getQueryParams())
                .pathParams(requestConfig.getPathParams())
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
    }

    /**
     * Performs a POST request with authentication.
     *
     * @param endpoint the API endpoint
     * @param requestConfig the request configuration
     * @return the response
     */
    protected Response post(String endpoint, RequestConfig requestConfig) {
        var request = given()
                .header("Authorization", "Bearer " + authToken)
                .queryParams(requestConfig.getQueryParams())
                .pathParams(requestConfig.getPathParams());

        if (requestConfig.getBody() != null) {
            request.contentType(ContentType.JSON).body(requestConfig.getBody());
        }

        return request
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();
    }

    /**
     * Performs a PUT request with authentication.
     *
     * @param endpoint the API endpoint
     * @param requestConfig the request configuration
     * @return the response
     */
    protected Response put(String endpoint, RequestConfig requestConfig) {
        var request = given()
                .header("Authorization", "Bearer " + authToken)
                .queryParams(requestConfig.getQueryParams())
                .pathParams(requestConfig.getPathParams());

        if (requestConfig.getBody() != null) {
            request.contentType(ContentType.JSON).body(requestConfig.getBody());
        }

        return request
                .when()
                .put(endpoint)
                .then()
                .extract()
                .response();
    }

    /**
     * Performs a DELETE request with authentication.
     *
     * @param endpoint the API endpoint
     * @param requestConfig the request configuration
     * @return the response
     */
    protected Response delete(String endpoint, RequestConfig requestConfig) {
        return given()
                .header("Authorization", "Bearer " + authToken)
                .queryParams(requestConfig.getQueryParams())
                .pathParams(requestConfig.getPathParams())
                .when()
                .delete(endpoint)
                .then()
                .extract()
                .response();
    }

    /**
     * Performs a request without authentication.
     *
     * @param endpoint the API endpoint
     * @param method the HTTP method
     * @param requestConfig the request configuration
     * @return the response
     */
    protected Response requestWithoutAuth(String endpoint, String method, RequestConfig requestConfig) {
        var request = given()
                .queryParams(requestConfig.getQueryParams())
                .pathParams(requestConfig.getPathParams());

        if (requestConfig.getBody() != null) {
            request.contentType(ContentType.JSON).body(requestConfig.getBody());
        }

        switch (method.toUpperCase()) {
            case "GET":
                return request.when().get(endpoint).then().extract().response();
            case "POST":
                return request.when().post(endpoint).then().extract().response();
            case "PUT":
                return request.when().put(endpoint).then().extract().response();
            case "DELETE":
                return request.when().delete(endpoint).then().extract().response();
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }
}
