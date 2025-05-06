package pl.where2play.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 * This exception is typically thrown when a resource with a specific ID does not exist.
 */
public class ResourceNotFoundException extends BaseException {
    
    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";
    
    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message, STATUS, ERROR_CODE);
    }
    
    /**
     * Constructs a new ResourceNotFoundException with a message indicating that a resource of the specified type with the specified ID was not found.
     *
     * @param resourceName the name of the resource type
     * @param id the ID of the resource
     */
    public ResourceNotFoundException(String resourceName, Object id) {
        super(String.format("%s not found with id: %s", resourceName, id), STATUS, ERROR_CODE);
    }
    
    /**
     * Constructs a new ResourceNotFoundException with a message indicating that a resource of the specified type with the specified field value was not found.
     *
     * @param resourceName the name of the resource type
     * @param fieldName the name of the field
     * @param fieldValue the value of the field
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue), STATUS, ERROR_CODE);
    }
    
    /**
     * Constructs a new ResourceNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause, STATUS, ERROR_CODE);
    }
}