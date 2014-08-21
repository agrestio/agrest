package com.nhl.link.rest;

/**
 * A helper to locate objects within a context of a single
 * {@link UpdateResponse}.
 * 
 * @since 1.4
 */
public interface ResponseObjectMapper<T> {

	boolean isIdempotent(EntityUpdate u);

	Object findParent();

	T find(EntityUpdate u);

	T create(EntityUpdate u);
}
