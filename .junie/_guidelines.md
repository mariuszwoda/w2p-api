# W2P API Development Guidelines

This document provides essential information for developers working on the W2P API project.

## Build and Configuration Instructions

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
- Detailed logging enabled
- CSRF protection disabled

#### Production Environment
- Uses PostgreSQL database
- Requires the following environment variables:
  - `POSTGRES_HOST` (default: localhost)
  - `POSTGRES_PORT` (default: 5432)
  - `POSTGRES_DB` (default: calendardb)
  - `POSTGRES_USER` (default: postgres)
  - `POSTGRES_PASSWORD` (default: password)
- CSRF protection enabled
- Connection pool configured with HikariCP

#### OAuth2 Configuration
For OAuth2 authentication, set the following environment variables:
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `FACEBOOK_CLIENT_ID`
- `FACEBOOK_CLIENT_SECRET`

#### JWT Configuration
For JWT authentication, set:
- `JWT_SECRET` (default: your-jwt-secret-key)
- JWT expiration is set to 24 hours (86400000 ms)

## Testing Information

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserRepositoryTest

# Run specific test method
mvn test -Dtest=UserRepositoryTest#testFindByEmail
```

### Test Structure
The project uses JUnit 5 for testing. Tests are organized by type:

1. **Unit Tests**: Test individual components in isolation
   - Located in the same package as the class being tested
   - Naming convention: `ClassNameTest.java`

2. **Repository Tests**: Test database interactions
   - Use `@DataJpaTest` annotation
   - Use `TestEntityManager` for test setup
   - Example: `UserRepositoryTest.java`

3. **Integration Tests**: Test multiple components together
   - Use `@SpringBootTest` annotation
   - Example: `W2pApiApplicationTests.java`

### Creating New Tests

#### Entity Test Example
```java
@Test
void testEntityCreation() {
    // Given
    String email = "test@example.com";
    
    // When
    User user = User.builder()
            .email(email)
            .build();
    
    // Then
    assertEquals(email, user.getEmail());
}
```

#### Repository Test Example
```java
@DataJpaTest
class RepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private YourRepository repository;
    
    @Test
    void testRepositoryMethod() {
        // Given
        YourEntity entity = new YourEntity();
        entityManager.persist(entity);
        entityManager.flush();
        
        // When
        Optional<YourEntity> found = repository.findById(entity.getId());
        
        // Then
        assertTrue(found.isPresent());
    }
}
```

#### Controller Test Example
```java
@WebMvcTest(YourController.class)
class ControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private YourService service;
    
    @Test
    void testEndpoint() throws Exception {
        // Given
        when(service.someMethod()).thenReturn(result);
        
        // When & Then
        mockMvc.perform(get("/your-endpoint"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.property").value(expectedValue));
    }
}
```

## Code Style and Development Guidelines

### Project Structure
- `model`: JPA entities
- `repository`: Spring Data JPA repositories
- `dto`: Data Transfer Objects
- `service`: Business logic
- `controller`: REST endpoints
- `security`: Authentication and authorization

### Coding Conventions
- Use Lombok annotations to reduce boilerplate code
- Follow the builder pattern for complex object creation
- Use Optional for nullable return values
- Use proper JavaDoc comments for public methods and classes

### Entity Relationships
- Use `@ToString.Exclude` and `@EqualsAndHashCode.Exclude` for bidirectional relationships to prevent infinite recursion
- Use `@Builder.Default` for collections that should be initialized

### Security Best Practices
- Never store sensitive information in plain text
- Use environment variables for sensitive configuration
- JWT tokens should have a reasonable expiration time
- Always validate user input

### Database Migrations
- Use Hibernate's auto-update for development only
- For production, consider using a migration tool like Flyway or Liquibase

### Logging
- Use appropriate log levels:
  - ERROR: For errors that need immediate attention
  - WARN: For potential issues that don't prevent the application from working
  - INFO: For important application events
  - DEBUG: For detailed information useful during development

### Testing Best Practices
- Follow the Given-When-Then pattern for test structure
- Test both positive and negative scenarios
- Use meaningful test names that describe what is being tested
- Keep tests independent of each other