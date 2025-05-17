# W2P Calendar API

A Spring Boot-based RESTful API for calendar events with Google Calendar integration. This API allows users to manage calendar events, synchronize with Google Calendar, and authenticate using OAuth2 providers like Google and Facebook.

## Features

- **Calendar Event Management**: Create, read, update, and delete calendar events
- **Google Calendar Integration**: Synchronize events with Google Calendar
- **OAuth2 Authentication**: Authenticate users via Google and Facebook
- **JWT-based Security**: Secure API endpoints with JWT tokens
- **Swagger Documentation**: Interactive API documentation
- **Multi-environment Support**: Development (H2) and Production (PostgreSQL) environments

## Technology Stack

- **Java 17**: Modern Java features
- **Spring Boot 3.4.5**: Latest Spring Boot framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database access
- **H2 Database**: In-memory database for development
- **PostgreSQL**: Production database
- **Swagger/OpenAPI**: API documentation
- **JWT**: JSON Web Tokens for authentication
- **Lombok**: Reduce boilerplate code
- **JUnit 5**: Testing framework
- **RestAssured**: End-to-end API testing

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL (for production) or H2 (for development)

### Building the Project

```bash
# Clone the repository
git clone <repository-url>
cd w2p-api

# Build the project
mvn clean install
```

### Running the Application

```bash
# Run with default profile (dev)
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring.profiles.active=prod
```

### Configuration

The application uses Spring profiles for environment-specific configuration:

#### Development Environment (default)
- Uses H2 in-memory database
- H2 console available at `/h2-console`
- Default credentials: username=sa, password=password

#### Production Environment
- Uses PostgreSQL database
- Requires environment variables for database connection

For OAuth2 authentication, set the following environment variables:
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `FACEBOOK_CLIENT_ID`
- `FACEBOOK_CLIENT_SECRET`

For JWT authentication, set:
- `JWT_SECRET` (default: your-jwt-secret-key)

## API Documentation

Once the application is running, you can access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

This provides interactive documentation for all API endpoints.

## API Endpoints

### Authentication
- `POST /api/auth/login`: Authenticate with OAuth2 token

### Users
- `GET /api/users/me`: Get current user profile
- `PUT /api/users/me`: Update current user profile
- `DELETE /api/users/me`: Delete current user account
- `GET /api/users/search`: Search for users
- `GET /api/users/{id}`: Get user by ID

### Calendar Events
- `GET /api/events`: Get all events for current user
- `POST /api/events`: Create a new event
- `GET /api/events/{id}`: Get event by ID
- `PUT /api/events/{id}`: Update an event
- `DELETE /api/events/{id}`: Delete an event
- `GET /api/events/range`: Get events in a date range
- `POST /api/events/sync`: Synchronize events with external calendar
- `POST /api/events/{eventId}/attendees/{userId}`: Add attendee to event
- `DELETE /api/events/{eventId}/attendees/{userId}`: Remove attendee from event

### Google Calendar
- `GET /api/google-calendar/auth-url`: Get Google Calendar authorization URL
- `POST /api/google-calendar/exchange-code`: Exchange authorization code for token
- `GET /api/google-calendar/auth-status`: Check Google Calendar authorization status

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserRepositoryTest

# Run specific test method
mvn test -Dtest=UserRepositoryTest#testFindByEmail
```

## Future Improvements

See [Future Improvements](src/main/resources/docs/future-improvements.md) for a roadmap of planned enhancements.

## License

This project is licensed under the MIT License - see the LICENSE file for details.