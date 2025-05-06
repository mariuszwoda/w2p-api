package pl.where2play.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Provides consistent error responses for all exceptions.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles custom BaseException and its subclasses.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        logError(ex, request);

        String requestId = (String) request.getAttribute("requestId");

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getStatus().value(),
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getTimestamp(),
                request.getRequestURI(),
                requestId
        );

        // Add validation errors if present (for InvalidRequestException)
        if (ex instanceof InvalidRequestException) {
            errorResponse.setValidationErrors(((InvalidRequestException) ex).getErrors());
        }

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    /**
     * Handles validation exceptions from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        logError(ex, request);

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                        (error1, error2) -> error1 + ", " + error2
                ));

        String requestId = (String) request.getAttribute("requestId");

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Validation failed",
                LocalDateTime.now(),
                request.getRequestURI(),
                requestId
        );
        errorResponse.setValidationErrors(errors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles constraint violation exceptions.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        logError(ex, request);

        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage(),
                        (error1, error2) -> error1 + ", " + error2
                ));

        String requestId = (String) request.getAttribute("requestId");

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Constraint violation",
                LocalDateTime.now(),
                request.getRequestURI(),
                requestId
        );
        errorResponse.setValidationErrors(errors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles type mismatch exceptions.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        logError(ex, request);

        String message = String.format("Parameter '%s' should be of type %s", 
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        String requestId = (String) request.getAttribute("requestId");

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "TYPE_MISMATCH",
                message,
                LocalDateTime.now(),
                request.getRequestURI(),
                requestId
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        // Don't log as error for favicon.ico requests
        if (request.getRequestURI().equals("/favicon.ico")) {
            log.debug("Favicon request not handled: {}", request.getRequestURI());
        } else {
            logError(ex, request);
        }

        String requestId = (String) request.getAttribute("requestId");

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "RESOURCE_NOT_FOUND",
                "The requested resource was not found",
                LocalDateTime.now(),
                request.getRequestURI(),
                requestId
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles ResourceNotFoundException specifically for 404 errors when a resource is not found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        logError(ex, request);

        String requestId = (String) request.getAttribute("requestId");

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getTimestamp(),
                request.getRequestURI(),
                requestId
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Fallback handler for all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, HttpServletRequest request) {
        logError(ex, request);

        String requestId = (String) request.getAttribute("requestId");

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                LocalDateTime.now(),
                request.getRequestURI(),
                requestId
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Logs error details including request information.
     */
    private void logError(Exception ex, HttpServletRequest request) {
        // Get request ID if available
        String requestId = (String) request.getAttribute("requestId");
        String requestIdInfo = requestId != null ? "[ID: " + requestId + "] " : "";

        // Get API info if available
        String apiInfo = (String) request.getAttribute("apiInfo");
        String apiInfoLog = apiInfo != null ? "[API: " + apiInfo + "] " : "";

        log.error("{}{}Exception occurred while processing request: {} {}",
                requestIdInfo, apiInfoLog, request.getMethod(), request.getRequestURI(), ex);

        // Log request details
        log.error("{}{}Request details: Method={}, URI={}, Parameters={}, Headers={}",
                requestIdInfo, apiInfoLog,
                request.getMethod(),
                request.getRequestURI(),
                request.getParameterMap(),
                getHeadersAsString(request));
    }

    /**
     * Extracts headers from the request as a string.
     */
    private String getHeadersAsString(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> 
                headers.put(headerName, request.getHeader(headerName)));
        return headers.toString();
    }

    /**
     * Error response class for consistent error responses.
     */
    public static class ErrorResponse {
        private final int status;
        private final String errorCode;
        private final String message;
        private final LocalDateTime timestamp;
        private final String path;
        private final String requestId;
        private Map<String, String> validationErrors;

        public ErrorResponse(int status, String errorCode, String message, LocalDateTime timestamp, String path) {
            this.status = status;
            this.errorCode = errorCode;
            this.message = message;
            this.timestamp = timestamp;
            this.path = path;
            this.requestId = null; // Default constructor for backward compatibility
        }

        public ErrorResponse(int status, String errorCode, String message, LocalDateTime timestamp, String path, String requestId) {
            this.status = status;
            this.errorCode = errorCode;
            this.message = message;
            this.timestamp = timestamp;
            this.path = path;
            this.requestId = requestId;
        }

        public int getStatus() {
            return status;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getPath() {
            return path;
        }

        public String getRequestId() {
            return requestId;
        }

        public Map<String, String> getValidationErrors() {
            return validationErrors;
        }

        public void setValidationErrors(Map<String, String> validationErrors) {
            this.validationErrors = validationErrors;
        }
    }
}
