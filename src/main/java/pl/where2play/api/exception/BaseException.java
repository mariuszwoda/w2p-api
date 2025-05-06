package pl.where2play.api.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Base exception class for all application-specific exceptions.
 * Provides common functionality and properties for all exceptions.
 */
public abstract class BaseException extends RuntimeException {
    
    private final HttpStatus status;
    private final String errorCode;
    private final LocalDateTime timestamp;
    
    /**
     * Constructs a new BaseException with the specified detail message, HTTP status, and error code.
     *
     * @param message the detail message
     * @param status the HTTP status to be returned
     * @param errorCode the error code
     */
    protected BaseException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructs a new BaseException with the specified detail message, cause, HTTP status, and error code.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     * @param status the HTTP status to be returned
     * @param errorCode the error code
     */
    protected BaseException(String message, Throwable cause, HttpStatus status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Returns the HTTP status associated with this exception.
     *
     * @return the HTTP status
     */
    public HttpStatus getStatus() {
        return status;
    }
    
    /**
     * Returns the error code associated with this exception.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Returns the timestamp when this exception was created.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}