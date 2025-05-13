# Where2Play API

## Overview

Where2Play API is a Spring Boot application that provides backend services for calendar event management. The API offers functionalities for creating, retrieving, updating, and deleting calendar events through RESTful endpoints.

## Technical Stack

- **Java**: Version 21
- **Spring Boot**: Version 3.4.4
- **Database Support**:
    - H2 Database (Development)
    - PostgreSQL (Production)
- **API Documentation**: OpenAPI/Swagger UI
- **Database Migration**: Liquibase
- **Testing**: JUnit 5, REST Assured
- **Build Tool**: Maven

## Project Structure

The project follows a standard Spring Boot application structure:

- `src/main/java`: Contains the application source code
    - `config`: Configuration classes
    - `controller`: REST controllers
    - `exception`: Custom exception handlers
    - `model`: Data model classes
    - `repository`: Data access layer
    - `service`: Business logic layer
- `src/main/resources`: Configuration files and resources
    - Environment-specific configuration files (dev, prod)
    - Database migration scripts
- `src/test/java`: Test classes
    - `controller`: Controller integration tests
    - `service`: Service unit tests
    - `test/e2e`: End-to-end API tests

## API Endpoints

The API provides endpoints for managing calendar events:

- **GET /api/events**: Get all events
- **GET /api/events/{id}**: Get event by ID
- **POST /api/events**: Create a new event
- **PUT /api/events/{id}**: Update an existing event
- **DELETE /api/events/{id}**: Delete an event

For detailed API documentation, access the Swagger UI at `/swagger-ui.html` when the application is running.

## Environment Configuration

The application supports multiple environments through Spring profiles:

- **Development (`dev`)**: Uses H2 in-memory database, suitable for local development
- **Production (`prod`)**: Uses PostgreSQL database, configured for deployment

Configuration files:
- `application-dev.yml`: Development environment settings
- `application-prod.yml`: Production environment settings

## Testing

The application includes various levels of testing:

### Unit Tests
Standard unit tests for service and utility classes.

### Integration Tests
Tests that verify the integration between controllers, services, and repositories.

### End-to-End Tests
Complete API tests that verify the application's behavior from the client's perspective using REST Assured. These tests can be configured to run against different environments by specifying profiles:

- E2E tests with dev profile: Tests against local development environment
- E2E tests with prod profile: Tests against production environment

#### Running Tests with Multiple Profiles

You can run tests with multiple Spring profiles to test against different environments:

```shell script
# Run tests with e2e and dev profiles
mvn test -Dspring.profiles.active=e2e,dev -Dgroups=E2ETest

# Run tests with e2e and prod profiles

mvn test -Dspring.profiles.active=e2e,prod -Dgroups=E2ETest
```


## Building and Running

### Prerequisites

- Java 21 JDK
- Maven

### Building the Application

```shell script
mvn clean install
```


### Running the Application

```shell script
# Run with development profile
mvn spring-boot:run -Dspring.profiles.active=dev

# Run with production profile
mvn spring-boot:run -Dspring.profiles.active=prod
```


## Deployment

The application can be deployed as a JAR file:

```shell script
java -jar target/w2p-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```


A GitHub Actions workflow is included for CI/CD deployment to Azure.

## License

See the LICENSE.txt file for details.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request