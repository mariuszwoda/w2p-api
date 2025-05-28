package pl.where2play.w2papi.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("test-request");
    }

    @Test
    void handleResourceNotFoundException() {
        // Given
        ResourceNotFoundException ex = ResourceNotFoundException.forResource("User", "id", 1L);

        // When
        ErrorResponse response = exceptionHandler.handleResourceNotFoundException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("User not found with id: 1", response.getMessage());
        assertEquals("test-request", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleIllegalArgumentException() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        // When
        ErrorResponse response = exceptionHandler.handleIllegalArgumentException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals("Invalid argument", response.getMessage());
        assertEquals("test-request", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleIllegalArgumentExceptionWithNotFoundMessage() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("User not found with id: 1");

        // When
        ErrorResponse response = exceptionHandler.handleIllegalArgumentException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("User not found with id: 1", response.getMessage());
        assertEquals("test-request", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleAccessDeniedException() {
        // Given
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        // When
        ErrorResponse response = exceptionHandler.handleAccessDeniedException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertEquals("You don't have permission to access this resource", response.getMessage());
        assertEquals("test-request", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleForbiddenException() {
        // Given
        ForbiddenException ex = ForbiddenException.forResource("Event", 1L);

        // When
        ErrorResponse response = exceptionHandler.handleForbiddenException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertEquals("You don't have permission to access Event with ID: 1", response.getMessage());
        assertEquals("test-request", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleUnauthorizedException() {
        // Given
        UnauthorizedException ex = UnauthorizedException.withDefaultMessage();

        // When
        ErrorResponse response = exceptionHandler.handleUnauthorizedException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals("Authentication is required to access this resource", response.getMessage());
        assertEquals("test-request", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleBadCredentialsException() {
        // Given
        BadCredentialsException ex = new BadCredentialsException("Invalid credentials");

        // When
        ErrorResponse response = exceptionHandler.handleBadCredentialsException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals("Authentication failed: Invalid credentials", response.getMessage());
        assertEquals("test-request", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleUsernameNotFoundException() {
        // Given
        UsernameNotFoundException ex = new UsernameNotFoundException("User not found");

        // When
        ErrorResponse response = exceptionHandler.handleUsernameNotFoundException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("User not found", response.getMessage());
        assertEquals("test-request", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleUnsupportedOperationException() {
        // Given
        UnsupportedOperationException ex = new UnsupportedOperationException("Operation not supported");

        // When
        ErrorResponse response = exceptionHandler.handleUnsupportedOperationException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals("Operation not supported", response.getMessage());
        assertEquals("test-request", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleValidationExceptions() {
        // Given
        MethodArgumentNotValidException ex = createMethodArgumentNotValidException();

        // When
        ValidationErrorResponse response = exceptionHandler.handleValidationExceptions(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals("Validation failed", response.getMessage());
        assertEquals("test-request", response.getPath());
        assertNotNull(response.getTimestamp());

        Map<String, String> errors = response.getErrors();
        assertNotNull(errors);
        assertEquals(2, errors.size());
        assertEquals("Name is required", errors.get("name"));
        assertEquals("Email must be valid", errors.get("email"));
    }

    @Test
    void handleAllExceptions() {
        // Given
        Exception ex = new Exception("Unexpected error");

        // When
        ErrorResponse response = exceptionHandler.handleAllExceptions(ex, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertEquals("An unexpected error occurred. Please try again later.", response.getMessage());
        assertEquals("test-request", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    private MethodArgumentNotValidException createMethodArgumentNotValidException() {
        // Create field errors
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("user", "name", "Name is required"));
        fieldErrors.add(new FieldError("user", "email", "Email must be valid"));

        // Create binding result with field errors
        BindingResult bindingResult = org.mockito.Mockito.mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));

        // Create MethodArgumentNotValidException with mocked getMessage
        MethodArgumentNotValidException ex = org.mockito.Mockito.mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(ex.getMessage()).thenReturn("Validation failed");

        return ex;
    }
}
