package pl.where2play.api.test.e2e.controller;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import pl.where2play.api.test.e2e.base.BaseApiTest;
import pl.where2play.api.test.e2e.config.TestConfig;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CalendarEventControllerApiTest extends BaseApiTest {

    @Autowired
    private TestConfig testConfig;

    @AfterAll
    void tearDown() {
        if (idCreated != null) {
            deleteFromTable("calendar_events", List.of(idCreated), "/api/events/{id}");
        }
    }

    static Long idCreated;

    private static Stream<Arguments> provideTestCases() {
        return Stream.of(
                // GET requests
                Arguments.of(
                        "GET",
                        "/api/events/" + idCreated,
                        null,  // no body
                        null,  // query param
                        null,  // token
                        null,  // no path params
                        null,  // no multipart
                        HttpStatus.OK.value()
                )

                // DELETE with path parameter
                /*
                Arguments.of(
                        "DELETE",
                        "/api/events/{id}",
                        null,  // no body
                        null,  // no query params
                        null,
                        new HashMap<String, Object>() {{
                            put("id", idCreated);
                        }},
                        null,  // no multipart
                        HttpStatus.NO_CONTENT.value()
                )
                */
        );
    }

    @Test
    @Order(1)
    void testCreate() {
        Response response = this.testEndpoint(
                "POST",
                "/api/events",
//                "",
                "{\"title\": \"test\", \"startTime\": \"" + LocalDateTime.now() + "\", \"endTime\": \"" + LocalDateTime.now() + "\"}",
                null,  // no query params
                null,
                null,  // no path params
                null,  // no multipart
                HttpStatus.CREATED.value()
        );
        idCreated = Long.valueOf(response.body().jsonPath().getString("id"));
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    @Order(2)
    void testCustom(
            String method,
            String path,
            String body,
            Map<String, Object> queryParams,
            Map<String, String> headers,
            Map<String, Object> pathParams,
            Map<String, Object> multipartParams,
            int expectedStatus
    ) {
        this.testEndpoint(method, path, body, queryParams, headers, pathParams, multipartParams, expectedStatus);
    }

    @Disabled
    @Test
    @Order(3)
    void testDelete() {
        // If idCreated is null (when running this test directly), create an event first
        if (idCreated == null) {
            testCreate();
        }
        // Use the generic deleteFromTable method from BaseApiTest
        deleteFromTable("calendar_events", Arrays.asList(idCreated), "/api/events/{id}");
    }
}
