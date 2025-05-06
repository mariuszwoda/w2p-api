package pl.where2play.api.exception;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when a request contains invalid data or parameters.
 * This exception is typically thrown during validation of request data.
 */
public class InvalidRequestException extends BaseException {
    
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String ERROR_CODE = "INVALID_REQUEST";
    
    private final Map<String, String> errors;
    
    /**
     * Constructs a new InvalidRequestException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidRequestException(String message) {
        super(message, STATUS, ERROR_CODE);
        this.errors = new HashMap<>();
    }
    
    /**
     * Constructs a new InvalidRequestException with the specified detail message and validation errors.
     *
     * @param message the detail message
     * @param errors a map of field names to error messages
     */
    public InvalidRequestException(String message, Map<String, String> errors) {
        super(message, STATUS, ERROR_CODE);
        this.errors = errors;
    }
    
    /**
     * Constructs a new InvalidRequestException with the specified detail message and a single validation error.
     *
     * @param message the detail message
     * @param field the name of the field with the error
     * @param errorMessage the error message for the field
     */
    public InvalidRequestException(String message, String field, String errorMessage) {
        super(message, STATUS, ERROR_CODE);
        this.errors = new HashMap<>();
        this.errors.put(field, errorMessage);
    }
    
    /**
     * Constructs a new InvalidRequestException with the specified detail message, cause, and validation errors.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     * @param errors a map of field names to error messages
     */
    public InvalidRequestException(String message, Throwable cause, Map<String, String> errors) {
        super(message, cause, STATUS, ERROR_CODE);
        this.errors = errors;
    }
    
    /**
     * Returns the validation errors.
     *
     * @return a map of field names to error messages
     */
    public Map<String, String> getErrors() {
        return errors;
    }
}