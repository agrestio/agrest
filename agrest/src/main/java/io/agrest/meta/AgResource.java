package io.agrest.meta;

import io.agrest.annotation.LinkType;

import java.util.Collection;

/**
 * @since 1.18
 * 
 * @param <T>
 *            The type of the resource entity model.
 */
public interface AgResource<T> {

	String getPath();

	LinkType getType();

	Collection<AgOperation> getOperations();

	AgEntity<T> getEntity();
}
