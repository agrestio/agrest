package com.nhl.link.rest.client;

/**
 * @since 2.0
 */
public class LinkRestClientException extends RuntimeException {

	private static final long serialVersionUID = 8027409723345873322L;

	public LinkRestClientException(String message) {
		super(message);
	}

	public LinkRestClientException(String message, Throwable cause) {
		super(message, cause);
	}
}
