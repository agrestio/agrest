package io.agrest;

import io.agrest.runtime.processor.update.UpdateContext;

/**
 * A strategy for mapping update operations to existing objects.
 * 
 * @since 1.7
 */
public interface ObjectMapperFactory {

	/**
	 * Returns a mapper to handle objects of a given response.
	 */
	<T, E> ObjectMapper<T, E> createMapper(UpdateContext<T, E> context);
}