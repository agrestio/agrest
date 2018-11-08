package io.agrest;

import javax.ws.rs.core.Response.Status;

/**
 * An exception that encapsulates Agrest error mapped to the HTTP status code. Used by the framework internally and can
 * also be used by the application code.
 */
public class AgException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Status status;

    public AgException() {
        this(Status.INTERNAL_SERVER_ERROR);
    }

    public AgException(Status status) {
        this(status, null, null);
    }

    public AgException(Status status, String message) {
        this(status, message, null);
    }

    public AgException(Status status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    /**
     * Constructs an exception with the specified message and an optional list of message
     * formatting arguments. Message formatting rules follow "String.format(..)"
     * conventions.
     */
    public AgException(String messageFormat, Object... messageArgs) {
        super(messageFormat == null ? null : String.format(messageFormat, messageArgs));
    }

    /**
     * Constructs an <code>AgException</code> that wraps
     * <code>exception</code> thrown elsewhere.
     */
    public AgException(Throwable cause) {
        super(cause);
    }

    public Status getStatus() {
        return status;
    }
}
