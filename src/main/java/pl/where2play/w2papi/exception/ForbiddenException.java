package pl.where2play.w2papi.exception;

/**
 * Exception thrown when a user doesn't have permission to access a resource.
 */
public class ForbiddenException extends RuntimeException {

    /**
     * Constructs a new ForbiddenException with the specified detail message.
     *
     * @param message the detail message
     */
    public ForbiddenException(String message) {
        super(message);
    }

    /**
     * Constructs a new ForbiddenException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ForbiddenException with a default message.
     *
     * @return the exception
     */
    public static ForbiddenException withDefaultMessage() {
        return new ForbiddenException("You don't have permission to access this resource");
    }

    /**
     * Constructs a new ForbiddenException for a specific resource.
     *
     * @param resourceType the type of resource
     * @param resourceId the ID of the resource
     * @return the exception
     */
    public static ForbiddenException forResource(String resourceType, Object resourceId) {
        return new ForbiddenException(String.format("You don't have permission to access %s with ID: %s", resourceType, resourceId));
    }
}