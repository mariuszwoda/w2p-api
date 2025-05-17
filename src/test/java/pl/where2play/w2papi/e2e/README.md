# E2E Test Framework

This framework provides a structured approach to writing end-to-end (E2E) tests for the Where2Play API. It follows best practices for test organization, reusability, and maintainability.

## Framework Structure

```
src/test/java/pl/where2play/w2papi/e2e/
├── controller/                  # Controller-specific E2E tests
│   ├── AuthControllerE2ETest.java
│   ├── CalendarEventControllerE2ETest.java
│   ├── GoogleCalendarControllerE2ETest.java
│   └── UserControllerE2ETest.java
├── framework/                   # Core framework classes
│   ├── BaseApiTest.java         # Base class for all E2E tests
│   ├── RequestConfig.java       # Configuration for API requests
│   ├── constants/               # Constants used across tests
│   │   └── ApiEndpoint.java    # API endpoint constants
│   ├── data/                    # Test data generation
│   │   └── TestDataService.java # Service for generating test data
│   └── util/                    # Utility classes
│       └── JsonUtils.java       # JSON handling utilities
└── README.md                    # This file
```

## Key Components

### BaseApiTest

The `BaseApiTest` class is the foundation of the E2E test framework. It provides:

- Common setup for all tests
- Methods for making HTTP requests (GET, POST, PUT, DELETE)
- Authentication handling
- Support for both local and remote testing

### RequestConfig

The `RequestConfig` class is used to configure API requests with:

- Query parameters
- Path parameters
- Request body

### TestDataService

The `TestDataService` class generates test data for E2E tests, including:

- Test users
- Test calendar events
- Test request DTOs

### JsonUtils

The `JsonUtils` class provides utilities for working with JSON files, including:

- Loading test data from JSON files
- Converting between objects and JSON

### ApiEndpoint

The `ApiEndpoint` class externalizes API endpoint constants to be used across tests.

## Writing Tests

### Basic Test Structure

```java
@Test
void testExample() {
    // 1. Prepare test data
    SomeRequest request = testDataService.createTestRequest();
    
    // 2. Make API request
    Response response = post(
            ApiEndpoint.SomeEndpoint.SOME_PATH,
            RequestConfig.withBody(request)
    );
    
    // 3. Verify response
    response.then()
            .statusCode(HttpStatus.OK.value())
            .body("someField", equalTo(expectedValue));
}
```

### Using JSON Files for Test Data

```java
@Test
void testWithJsonData() {
    // 1. Load test data from JSON file
    SomeRequest request = jsonUtils.loadFromClasspath(
            "test-data/some-request.json", 
            SomeRequest.class
    );
    
    // 2. Make API request
    Response response = post(
            ApiEndpoint.SomeEndpoint.SOME_PATH,
            RequestConfig.withBody(request)
    );
    
    // 3. Verify response
    response.then()
            .statusCode(HttpStatus.OK.value())
            .body("someField", equalTo(request.getSomeField()));
}
```

### Parameterized Tests

```java
@ParameterizedTest
@ValueSource(strings = {"VALUE1", "VALUE2"})
void testWithParameters(String parameter) {
    // Test implementation using the parameter
}
```

## Running Tests

### Locally

To run tests against a local instance of the application:

```bash
./mvnw test -Dtest=*E2ETest
```

### Against Remote Environment

To run tests against a remote environment:

1. Configure the remote URL in `application-test.properties`:
   ```properties
   api.remote-url=https://your-remote-api.example.com
   ```

2. Run tests with the test profile:
   ```bash
   ./mvnw test -Dtest=*E2ETest -Dspring.profiles.active=test
   ```

## Extending the Framework

### Adding New Controller Tests

1. Create a new test class in the `controller` package
2. Extend `BaseApiTest`
3. Implement tests for each endpoint

### Adding New Test Data

1. Add methods to `TestDataService` for generating new types of test data
2. Or add JSON files to `src/test/resources/test-data/`

### Adding New Endpoints

1. Add new endpoint constants to `ApiEndpoint`