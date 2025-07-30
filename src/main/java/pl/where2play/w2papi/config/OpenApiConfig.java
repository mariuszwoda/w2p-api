package pl.where2play.w2papi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI documentation.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configure the OpenAPI documentation.
     *
     * @return the OpenAPI configuration
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("W2P Calendar API")
                        .description("API for managing calendar events with integration support for Google Calendar, " +
                                "Apple Calendar, and Microsoft Calendar. Features include event creation, updating, " +
                                "deletion, sharing, and synchronization with external calendar providers. " +
                                "The API supports authentication via Google and Facebook OAuth2, and implements " +
                                "role-based access control for different types of users (admin, regular user, E2E test user).")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("W2P Team")
                                .email("contact@where2play.pl")
                                .url("https://where2play.pl"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme())
                        .addExamples("authRequest", createAuthRequestExample())
                        .addExamples("authResponse", createAuthResponseExample())
                        .addExamples("createEventRequest", createEventRequestExample())
                        .addExamples("eventResponse", createEventResponseExample()));
    }

    /**
     * Create a security scheme for JWT authentication.
     *
     * @return the security scheme
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    /**
     * Create an example for authentication request.
     *
     * @return the example
     */
    private Example createAuthRequestExample() {
        return new Example()
                .value("{\n" +
                        "  \"provider\": \"GOOGLE\",\n" +
                        "  \"token\": \"ya29.a0AfB_byC...\"\n" +
                        "}");
    }

    /**
     * Create an example for authentication response.
     *
     * @return the example
     */
    private Example createAuthResponseExample() {
        return new Example()
                .value("{\n" +
                        "  \"token\": \"eyJhbGciOiJIUzI1NiJ9...\",\n" +
                        "  \"user\": {\n" +
                        "    \"id\": 1,\n" +
                        "    \"email\": \"user@example.com\",\n" +
                        "    \"name\": \"Example User\",\n" +
                        "    \"pictureUrl\": \"https://example.com/profile.jpg\",\n" +
                        "    \"provider\": \"GOOGLE\"\n" +
                        "  }\n" +
                        "}");
    }

    /**
     * Create an example for create event request.
     *
     * @return the example
     */
    private Example createEventRequestExample() {
        return new Example()
                .value("{\n" +
                        "  \"title\": \"Team Meeting\",\n" +
                        "  \"description\": \"Weekly team sync-up\",\n" +
                        "  \"location\": \"Conference Room A\",\n" +
                        "  \"start\": \"2023-12-01T10:00:00\",\n" +
                        "  \"end\": \"2023-12-01T11:00:00\",\n" +
                        "  \"allDay\": false,\n" +
                        "  \"recurrence\": \"WEEKLY\",\n" +
                        "  \"attendees\": [2, 3],\n" +
                        "  \"reminders\": [15, 30]\n" +
                        "}");
    }

    /**
     * Create an example for event response.
     *
     * @return the example
     */
    private Example createEventResponseExample() {
        return new Example()
                .value("{\n" +
                        "  \"id\": 1,\n" +
                        "  \"title\": \"Team Meeting\",\n" +
                        "  \"description\": \"Weekly team sync-up\",\n" +
                        "  \"location\": \"Conference Room A\",\n" +
                        "  \"start\": \"2023-12-01T10:00:00\",\n" +
                        "  \"end\": \"2023-12-01T11:00:00\",\n" +
                        "  \"allDay\": false,\n" +
                        "  \"recurrence\": \"WEEKLY\",\n" +
                        "  \"owner\": {\n" +
                        "    \"id\": 1,\n" +
                        "    \"email\": \"user@example.com\",\n" +
                        "    \"name\": \"Example User\"\n" +
                        "  },\n" +
                        "  \"attendees\": [\n" +
                        "    {\n" +
                        "      \"id\": 2,\n" +
                        "      \"email\": \"attendee1@example.com\",\n" +
                        "      \"name\": \"Attendee One\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"id\": 3,\n" +
                        "      \"email\": \"attendee2@example.com\",\n" +
                        "      \"name\": \"Attendee Two\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"reminders\": [15, 30],\n" +
                        "  \"createdAt\": \"2023-11-25T09:30:00\",\n" +
                        "  \"updatedAt\": \"2023-11-25T09:30:00\"\n" +
                        "}");
    }
}
