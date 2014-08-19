package com.nhl.link.rest;

/**
 * Stores request-constraining parameters. During each request LinkRest would
 * apply {@link DataResponseConstraints} to client configuration, not allowing
 * the client to request more data than constraints allow (a client can request
 * *less* data of course).
 * 
 * @since 1.1
 */
public class DataResponseConstraints {

	private int fetchOffset;
	private int fetchLimit;
	private EntityConstraintsBuilder entityConstraints;

	/**
	 * @since 1.2
	 */
	public DataResponseConstraints(EntityConstraintsBuilder entityConstraints) {
		this.entityConstraints = entityConstraints;
	}

	public DataResponseConstraints fetchOffset(int fetchOffset) {
		this.fetchOffset = fetchOffset;
		return this;
	}

	public DataResponseConstraints fetchLimit(int fetchLimit) {
		this.fetchLimit = fetchLimit;
		return this;
	}

	public EntityConstraintsBuilder getEntityConstraints() {
		return entityConstraints;
	}

	public int getFetchOffset() {
		return fetchOffset;
	}

	public int getFetchLimit() {
		return fetchLimit;
	}
}
