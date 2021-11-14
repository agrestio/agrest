package io.agrest;

/**
 * A base response object in Agrest.
 * 
 * @since 1.19
 */
public abstract class AgResponse {
	
	protected int status;

	/**
	 * @since 4.7
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @since 4.7
	 */
	public void setStatus(int status) {
		this.status = status;
	}
}
