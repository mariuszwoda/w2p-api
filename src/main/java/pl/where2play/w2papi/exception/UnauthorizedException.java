package pl.where2play.w2papi.exception;

/**
 * Exception thrown when a user is not authenticated.
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Constructs a new UnauthorizedException with the specified detail message.
     *
     * @param message the detail message
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * Constructs a new UnauthorizedException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new UnauthorizedException with a default message.
     *
     * @return the exception
     */
    public static UnauthorizedException withDefaultMessage() {
        return new UnauthorizedException("Authentication is required to access this resource");
    }
}