package io.agrest;

import io.agrest.base.protocol.Exp;

/**
 * A helper to locate objects within a context of a single update request.
 * 
 * @since 1.7
 */
public interface ObjectMapper<T> {

	Object keyForObject(T object);

	Object keyForUpdate(EntityUpdate<T> update);

	Exp expressionForKey(Object key);
}
