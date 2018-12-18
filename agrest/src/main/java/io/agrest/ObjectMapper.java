package io.agrest;


/**
 * A helper to locate objects within a context of a single update request.
 * 
 * @since 1.7
 */
public interface ObjectMapper<T, E> {

	Object keyForObject(T object);

	Object keyForUpdate(EntityUpdate<T> update);

	E expressionForKey(Object key);
}
