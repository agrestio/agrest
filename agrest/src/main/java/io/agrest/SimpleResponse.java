package io.agrest;

/**
 * A response object that represents a 'Simple Document' from LinkRest protocol .
 */
public class SimpleResponse extends AgResponse {

	protected boolean success;
	protected String message;

	public SimpleResponse(boolean success) {
		this(success, null);
	}

	public SimpleResponse(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public boolean isSuccess() {
		return success;
	}

}
