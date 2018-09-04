package io.agrest;

/**
 * Stores request-constraining parameters. During each request AgREST would
 * apply {@link SizeConstraints} to client configuration, not allowing the
 * client to request more data than constraints allow (a client can request
 * *less* data of course).
 * 
 * @since 1.3
 */
public class SizeConstraints {

	private int fetchOffset;
	private int fetchLimit;

	public SizeConstraints fetchOffset(int fetchOffset) {
		this.fetchOffset = fetchOffset;
		return this;
	}

	public SizeConstraints fetchLimit(int fetchLimit) {
		this.fetchLimit = fetchLimit;
		return this;
	}

	public int getFetchOffset() {
		return fetchOffset;
	}

	public int getFetchLimit() {
		return fetchLimit;
	}
}
