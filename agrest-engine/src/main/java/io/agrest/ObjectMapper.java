package io.agrest;

import org.apache.cayenne.exp.Expression;

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
