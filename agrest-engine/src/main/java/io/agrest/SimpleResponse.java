package io.agrest;

/**
 * A response object that represents a 'Simple Document' from Agrest protocol .
 */
public class SimpleResponse extends AgResponse {

	protected final boolean success;
	protected final String message;

	/**
	 * @since 5.0
	 */
	public static SimpleResponse of(int status, boolean success) {
		return new SimpleResponse(status, success, null);
	}

	/**
	 * @since 5.0
	 */
	public static SimpleResponse of(int status, boolean success, String message) {
		return new SimpleResponse(status, success, message);
	}

	protected SimpleResponse(int status, boolean success, String message) {
		super(status);
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
