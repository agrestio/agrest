package io.agrest;

/**
 * A response object that represents a 'Simple Document' from Agrest protocol .
 */
public class SimpleResponse extends AgResponse {

	protected final String message;

	/**
	 * @since 5.0
	 */
	public static SimpleResponse of(int status) {
		return new SimpleResponse(status, null);
	}

	/**
	 * @since 5.0
	 */
	public static SimpleResponse of(int status, String message) {
		return new SimpleResponse(status, message);
	}

	protected SimpleResponse(int status, String message) {
		super(status);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
