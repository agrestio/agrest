package com.nhl.link.rest;

import javax.ws.rs.core.Response.Status;

public class SimpleResponse {

	protected Status status;
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

	/**
	 * @since 1.19
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @since 1.19
	 */
	public void setStatus(Status status) {
		this.status = status;
	}
}
