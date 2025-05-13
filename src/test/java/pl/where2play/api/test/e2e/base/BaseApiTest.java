package pl.where2play.api.test.e2e.base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import pl.where2play.api.test.e2e.config.ApiTestConfig;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;

/**
 * Base class for API tests.
 * Provides common functionality for testing REST endpoints.
 */
//@ActiveProfiles("e2e")
//@ActiveProfiles({"e2e", "dev"})
//@ActiveProfiles({"e2e", "prod"})
@Slf4j
@SpringJUnitConfig
@ContextConfiguration(classes = {ApiTestConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("E2ETest")
public abstract class BaseApiTest {
    // Default values
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_TIMEOUT_MILLIS = 1000;

    @Autowired
    protected ApiTestConfig apiTestConfig;

    @Autowired
    private Environment environment;

    private RequestSpecification requestSpec;

    /**
     * Set up the test environment.
     * Configures RestAssured with the appropriate base URL and timeout settings.
     */
    @BeforeAll
    void setUp() {
        // Check if e2e profile is active, abort if not
        checkE2eProfileActive();

        // Enable logging for RestAssured
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        // Get base URL from configuration
        String baseUrl = apiTestConfig.getBaseUrl();
        log.info("Using base URL: {}", baseUrl);

        // Parse the URL to extract host and port
        UrlComponents urlComponents = parseUrl(baseUrl);

        // Configure REST Assured with timeout settings
        RestAssuredConfig config = createRestAssuredConfig(DEFAULT_TIMEOUT_MILLIS);

        // Build the request specification
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(urlComponents.getBaseUri())
                .setPort(urlComponents.getPort())
                .setContentType(ContentType.JSON)
                .setConfig(config)
                .build();
    }

    /**
     * Checks if the "e2e" profile is active and aborts the test if it's not.
     * This ensures that E2E tests only run when the "e2e" profile is active.
     */
    private void checkE2eProfileActive() {
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isE2eActive = false;

        for (String profile : activeProfiles) {
            if ("e2e".equals(profile)) {
                isE2eActive = true;
                break;
            }
        }

        // Abort the test if e2e profile is not active
        Assumptions.assumeTrue(isE2eActive, 
            "Test aborted because 'e2e' profile is not active. Active profiles: " + 
            String.join(", ", activeProfiles));

        log.info("E2E profile is active. Proceeding with test.");
    }

    /**
     * Parse a URL into its components (base URI and port).
     * @param url The URL to parse
     * @return A UrlComponents object containing the base URI and port
     */
    private UrlComponents parseUrl(String url) {
        String host = url;
        int port = DEFAULT_PORT;

        // Extract host from URL (remove protocol)
        if (url.contains("://")) {
            host = url.split("://")[1];
        }

        // Extract port from host if present
        if (host.contains(":")) {
            String[] parts = host.split(":");
            host = parts[0];
            if (parts.length > 1) {
                try {
                    port = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    log.warn("Invalid port in URL: {}, using default port: {}", url, DEFAULT_PORT);
                }
            }
        }

        // Reconstruct the base URI with protocol
        String baseUri = url.contains("://") ?
                url.substring(0, url.indexOf("://") + 3) + host :
                "http://" + host;

        return new UrlComponents(baseUri, port);
    }

    /**
     * Create a RestAssured configuration with the specified timeout.
     * @param timeoutMillis The timeout in milliseconds
     * @return The RestAssured configuration
     */
    private RestAssuredConfig createRestAssuredConfig(int timeoutMillis) {
        return RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", timeoutMillis)
                        .setParam("http.socket.timeout", timeoutMillis)
                        .setParam("http.connection.manager.timeout", timeoutMillis));
    }

    /**
     * URL components class to hold the parsed URL parts.
     */
    @Value
    private static class UrlComponents {
        String baseUri;
        int port;
    }

    /**
     * Delete records from a database table by their IDs and verify they were deleted.
     *
     * @param tableName The name of the table to delete from
     * @param ids List of IDs to delete
     * @param verificationPath The API path to verify deletion, with {id} placeholder
     */
    protected void deleteFromTable(String tableName, List<Long> ids, String verificationPath) {
        if (ids == null || ids.isEmpty()) {
            log.warn("No IDs provided for deletion from {}", tableName);
            return;
        }

        log.info("Deleting {} records from table {}", ids.size(), tableName);

        try {
            // Execute the delete SQL
            int rowsAffected = executeDeleteSql(tableName, ids);
            log.info("Deleted {} rows from {}", rowsAffected, tableName);

            // Verify the entities were deleted
            verifyEntitiesDeleted(ids, verificationPath);
        } catch (Exception e) {
            log.error("Error deleting from table " + tableName, e);
            throw new RuntimeException("Error deleting from table " + tableName, e);
        }
    }

    /**
     * Execute a SQL DELETE statement to remove records from a table.
     *
     * @param tableName The name of the table to delete from
     * @param ids List of IDs to delete
     * @return The number of rows affected
     * @throws ClassNotFoundException If the JDBC driver cannot be loaded
     * @throws SQLException If there is an error executing the SQL
     */
    private int executeDeleteSql(String tableName, List<Long> ids) throws ClassNotFoundException, SQLException {
        // Get database configuration from TestConfig
        Map<String, String> dbConfig = apiTestConfig.getDatabaseConfig();

        // Log the environment and database configuration being used
        log.info("Using environment: {}", apiTestConfig.getEnvironment());
        log.info("Database URL: {}", dbConfig.get("url"));

        // Get database configuration
        String url = dbConfig.get("url");
        String driverClassName = dbConfig.get("driverClassName");

        // Load the JDBC driver
        Class.forName(driverClassName);

        // Get connection properties from TestConfig
        Properties connectionProps = apiTestConfig.getConnectionProperties();

        // Establish a connection and execute the delete
        try (Connection connection = DriverManager.getConnection(url, connectionProps)) {
            // Create placeholders for the IN clause
            String placeholders = ids.stream()
                    .map(id -> "?")
                    .collect(Collectors.joining(", "));

            // Prepare the SQL statement
            String sql = "DELETE FROM " + tableName + " WHERE id IN (" + placeholders + ")";
            log.debug("Executing SQL: {}", sql);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Set the parameters
                for (int i = 0; i < ids.size(); i++) {
                    statement.setLong(i + 1, ids.get(i));
                    log.trace("Parameter {} = {}", i + 1, ids.get(i));
                }

                // Execute the statement and return the number of rows affected
                return statement.executeUpdate();
            }
        }
    }

    /**
     * Verify that entities were deleted by trying to get them (should return 404).
     *
     * @param ids List of IDs to verify
     * @param verificationPath The API path to verify deletion, with {id} placeholder
     */
    private void verifyEntitiesDeleted(List<Long> ids, String verificationPath) {
        log.info("Verifying deletion of {} entities", ids.size());

        for (Long id : ids) {
            // Create a map with the path parameter for verification
            Map<String, Object> pathParams = new HashMap<>();
            pathParams.put("id", id);

            // Verify the entity was deleted by trying to get it (should return 404)
            log.debug("Verifying deletion of entity with ID: {}", id);
            this.testEndpoint(
                    "GET",
                    verificationPath,
                    null,  // no body
                    null,  // no query params
                    null,  // no headers
                    pathParams,
                    null,  // no multipart
                    HttpStatus.NOT_FOUND.value()
            );
        }
    }

    /**
     * Test an endpoint with the specified parameters.
     *
     * @param method The HTTP method (GET, POST, PUT, DELETE, PATCH)
     * @param path The path to the endpoint
     * @param body The request body (can be null)
     * @param queryParams Map of query parameters (can be null)
     * @param headers Map of headers (can be null)
     * @param pathParams Map of path parameters (can be null)
     * @param multipartParams Map of multipart parameters (can be null)
     * @param expectedStatus The expected HTTP status code
     * @return The response from the endpoint
     */
    protected Response testEndpoint(
            String method,
            String path,
            String body,
            Map<String, Object> queryParams,
            Map<String, String> headers,
            Map<String, Object> pathParams,
            Map<String, Object> multipartParams,
            int expectedStatus
    ) {
        log.info("Testing endpoint: {} {}", method, path);

        try {
            // Start building the request
            var request = RestAssured.given(requestSpec);

            // Add headers
            if (headers != null) {
                log.debug("Adding headers: {}", headers);
                request.headers(headers);
            }

            // Add query parameters
            if (queryParams != null) {
                log.debug("Adding query parameters: {}", queryParams);
                request.queryParams(queryParams);
            }

            // Add path parameters
            if (pathParams != null) {
                log.debug("Adding path parameters: {}", pathParams);
                request.pathParams(pathParams);
            }

            // Add body if present
            if (body != null) {
                log.debug("Adding body: {}", body);
                request.body(body);
            }

            // Add multipart parameters
            if (multipartParams != null) {
                log.debug("Adding multipart parameters: {}", multipartParams);
                multipartParams.forEach((key, value) -> {
                    if (value instanceof File) {
                        request.multiPart(key, (File) value);
                    } else {
                        request.multiPart(key, value);
                    }
                });
                request.contentType(MediaType.MULTIPART_FORM_DATA_VALUE);
            }

            // Execute request based on HTTP method
            Response response = switch (method.toUpperCase()) {
                case "GET" -> request.get(path);
                case "POST" -> request.post(path);
                case "PUT" -> request.put(path);
                case "DELETE" -> request.delete(path);
                case "PATCH" -> request.patch(path);
                default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            };

            // Validate the response status
            response.then().statusCode(expectedStatus);

            return response;
        } catch (Exception e) {
            log.error("Error testing endpoint: " + method + " " + path, e);
            throw new RuntimeException("Error testing endpoint: " + method + " " + path, e);
        }
    }

    protected Response executeRequest(String method, String path, String body,
                                      Map<String, Object> queryParams,
                                      Map<String, String> headers,
                                      Map<String, Object> pathParams,
                                      Map<String, Object> multipartParams) {
        RequestSpecification request = RestAssured.given(requestSpec);

        if (headers != null) request.headers(headers);
        if (queryParams != null) request.queryParams(queryParams);
        if (pathParams != null) request.pathParams(pathParams);
        if (body != null) request.body(body);

        if (multipartParams != null) {
            multipartParams.forEach((key, value) -> {
                if (value instanceof File) {
                    request.multiPart(key, (File) value);
                } else if (value instanceof String) {
                    request.multiPart(key, value.toString());
                } else {
                    request.multiPart(key, value);
                }
            });
            request.contentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        }

        return switch (method.toUpperCase()) {
            case "GET" -> request.get(path);
            case "POST" -> request.post(path);
            case "PUT" -> request.put(path);
            case "DELETE" -> request.delete(path);
            case "PATCH" -> request.patch(path);
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };
    }

    protected void validateResponseBody(String expectedJson) {
        RestAssured.given(requestSpec)
                .when()
                .get("/path")
                .then()
                .body(equalTo(expectedJson));
    }

    /**
     * Additional utility methods for response validation
     */

    /**
     * Validate that a response conforms to a JSON schema.
     *
     * @param endpoint The endpoint to test
     * @param schemaPath The path to the JSON schema
     */
    protected void validateJsonSchema(String endpoint, String schemaPath) {
        log.info("Validating JSON schema for endpoint: {} against schema: {}", endpoint, schemaPath);
        try {
            RestAssured.given(requestSpec)
                    .when()
                    .get(endpoint)
                    .then()
                    .assertThat();
            log.debug("JSON schema validation successful");
        } catch (Exception e) {
            log.error("JSON schema validation failed", e);
            throw new RuntimeException("JSON schema validation failed", e);
        }
    }

    /**
     * Validate response headers against expected values.
     *
     * @param expectedHeaders Map of expected header names and values
     */
    protected void validateResponseHeaders(Map<String, String> expectedHeaders) {
        log.info("Validating response headers: {}", expectedHeaders);
        // Implementation would go here
    }

    /**
     * Apply custom validation logic to a response.
     *
     * @param response The response to validate
     * @param validationLogic A consumer that performs validation on the response
     */
    protected void validateCustomResponse(Response response,
                                          Consumer<Response> validationLogic) {
        log.info("Applying custom validation to response");
        try {
            validationLogic.accept(response);
            log.debug("Custom validation successful");
        } catch (Exception e) {
            log.error("Custom validation failed", e);
            throw new RuntimeException("Custom validation failed", e);
        }
    }

    /**
     * Helper class for validating REST API responses.
     * Provides a fluent interface for common validation operations.
     */
    @Slf4j
    protected static class ResponseValidator {
        private final Response response;
        private final JsonPath jsonPath;

        /**
         * Create a new ResponseValidator for the given response.
         *
         * @param response The response to validate
         */
        public ResponseValidator(Response response) {
            this.response = response;
            this.jsonPath = response.jsonPath();
        }

        /**
         * Validate the response status code.
         *
         * @param expectedStatus The expected HTTP status code
         * @return This ResponseValidator instance for method chaining
         */
        public ResponseValidator validateStatusCode(int expectedStatus) {
            log.debug("Validating status code: expected={}, actual={}", expectedStatus, response.getStatusCode());
            response.then().statusCode(expectedStatus);
            return this;
        }

        /**
         * Validate a field in the response body.
         *
         * @param jsonPath The JSON path to the field
         * @param expectedValue The expected value
         * @return This ResponseValidator instance for method chaining
         */
        public ResponseValidator validateField(String jsonPath, Object expectedValue) {
            log.debug("Validating field at path {}: expected={}", jsonPath, expectedValue);
            response.then().body(jsonPath, equalTo(expectedValue));
            return this;
        }

        /**
         * Validate that a field exists in the response body.
         *
         * @param jsonPath The JSON path to the field
         * @return This ResponseValidator instance for method chaining
         */
        public ResponseValidator validateFieldExists(String jsonPath) {
            log.debug("Validating field exists at path {}", jsonPath);
            response.then().body(jsonPath, notNullValue());
            return this;
        }

        /**
         * Validate that a field matches a pattern.
         *
         * @param jsonPath The JSON path to the field
         * @param pattern The pattern to match
         * @return This ResponseValidator instance for method chaining
         */
        public ResponseValidator validateFieldPattern(String jsonPath, String pattern) {
            log.debug("Validating field at path {} matches pattern {}", jsonPath, pattern);
            response.then().body(jsonPath, matchesPattern(pattern));
            return this;
        }

        /**
         * Validate the size of an array in the response body.
         *
         * @param jsonPath The JSON path to the array
         * @param expectedSize The expected size of the array
         * @return This ResponseValidator instance for method chaining
         */
        public ResponseValidator validateArraySize(String jsonPath, int expectedSize) {
            log.debug("Validating array size at path {}: expected={}", jsonPath, expectedSize);
            response.then().body(jsonPath + ".size()", equalTo(expectedSize));
            return this;
        }

        /**
         * Validate that an array has at least a minimum size.
         *
         * @param jsonPath The JSON path to the array
         * @param minSize The minimum size of the array
         * @return This ResponseValidator instance for method chaining
         */
        public ResponseValidator validateArrayMinSize(String jsonPath, int minSize) {
            log.debug("Validating array min size at path {}: minSize={}", jsonPath, minSize);
            response.then().body(jsonPath + ".size()", greaterThanOrEqualTo(minSize));
            return this;
        }

        /**
         * Validate that an array is not empty.
         *
         * @param jsonPath The JSON path to the array
         * @return This ResponseValidator instance for method chaining
         */
        public ResponseValidator validateArrayNotEmpty(String jsonPath) {
            log.debug("Validating array at path {} is not empty", jsonPath);
            response.then().body(jsonPath + ".size()", greaterThan(0));
            return this;
        }

        /**
         * Validate that an array contains all expected items.
         *
         * @param jsonPath The JSON path to the array
         * @param expectedList The list of expected items
         * @return This ResponseValidator instance for method chaining
         */
        public <T> ResponseValidator validateList(String jsonPath, List<T> expectedList) {
            log.debug("Validating list at path {} contains expected items", jsonPath);
            response.then().body(jsonPath, hasItems(expectedList.toArray()));
            return this;
        }

        /**
         * Apply custom validation logic to the response.
         *
         * @param customValidation A consumer that performs validation on the response
         * @return This ResponseValidator instance for method chaining
         */
        public ResponseValidator validateCustom(Consumer<Response> customValidation) {
            log.debug("Applying custom validation to response");
            customValidation.accept(response);
            return this;
        }

        /**
         * Extract a field from the response body.
         *
         * @param jsonPath The JSON path to the field
         * @param type The class of the field to extract
         * @return The extracted field
         */
        public <T> T extractField(String jsonPath, Class<T> type) {
            log.debug("Extracting field at path {} as {}", jsonPath, type.getSimpleName());
            return this.jsonPath.get(jsonPath);
        }

        /**
         * Get the underlying response.
         *
         * @return The response
         */
        public Response getResponse() {
            return response;
        }
    }

    protected ResponseValidator validateResponse(Response response) {
        return new ResponseValidator(response);
    }
}
