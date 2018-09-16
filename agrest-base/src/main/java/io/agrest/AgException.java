package io.agrest;

import javax.ws.rs.core.Response.Status;

/**
 * An exception that encapsulates AgREST error mapped to the HTTP status code. Used by the framework internally and can
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

    public Status getStatus() {
        return status;
    }
}
