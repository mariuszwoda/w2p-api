package pl.where2play.w2papi.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Error response for validation errors.
 * Extends the standard error response to include field-specific validation errors.
 */
@Getter
@Setter
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> errors;

    /**
     * Constructor for ValidationErrorResponse.
     *
     * @param status the HTTP status code
     * @param message the error message
     * @param path the request path
     * @param timestamp the timestamp
     * @param errors the validation errors
     */
    public ValidationErrorResponse(int status, String message, String path, LocalDateTime timestamp, Map<String, String> errors) {
        super(status, message, path, timestamp);
        this.errors = errors;
    }
}