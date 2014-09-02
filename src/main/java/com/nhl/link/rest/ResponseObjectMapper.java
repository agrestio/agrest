package com.nhl.link.rest;

import java.util.Map;

/**
 * A helper to locate objects within a context of a single
 * {@link UpdateResponse}.
 * 
 * @since 1.4
 */
public interface ResponseObjectMapper<T> {

	Object findParent();

	/**
	 * Returns a map of objects to updates for all updates included in this
	 * request.
	 * 
	 * @since 1.7
	 */
	Map<EntityUpdate, T> find();

	T create(EntityUpdate u);

	boolean isIdempotent(EntityUpdate u);
}
