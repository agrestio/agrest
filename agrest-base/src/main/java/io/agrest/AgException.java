package io.agrest;

/**
 * Encapsulates an Agrest error condition described by an HTTP status code. Used by the framework internally and can
 * also be used by the application code.
 *
 * @see HttpStatus
 */
public class AgException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;

    public AgException() {
        this(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public AgException(int status) {
        this(status, null, null);
    }

    public AgException(int status, String message) {
        this(status, message, null);
    }

    public AgException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
