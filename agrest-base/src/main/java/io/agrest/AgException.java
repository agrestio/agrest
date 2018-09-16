package io.agrest;

import javax.ws.rs.core.Response.Status;

/**
 * An exception that renders ExtJS-friendly JSON response. Can be used by the
 * application code and is also used by the framework internally.
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
