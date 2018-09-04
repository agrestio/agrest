package io.agrest.client;

/**
 * @since 2.0
 */
public class AgRESTClientException extends RuntimeException {

	private static final long serialVersionUID = 8027409723345873322L;

	public AgRESTClientException(String message) {
		super(message);
	}

	public AgRESTClientException(String message, Throwable cause) {
		super(message, cause);
	}
}
