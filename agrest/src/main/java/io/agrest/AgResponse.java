package io.agrest;

import javax.ws.rs.core.Response.Status;

/**
 * A base response object in AgREST.
 * 
 * @since 1.19
 */
public abstract class AgResponse {
	
	protected Status status;

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
