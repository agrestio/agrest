package io.agrest;

import io.agrest.backend.exp.Expression;

/**
 * A helper to locate objects within a context of a single update request.
 * 
 * @since 1.7
 */
public interface ObjectMapper<T> {

	Object keyForObject(T object);

	Object keyForUpdate(EntityUpdate<T> update);

	Expression expressionForKey(Object key);
}
