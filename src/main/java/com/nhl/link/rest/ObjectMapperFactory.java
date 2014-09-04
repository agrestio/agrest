package com.nhl.link.rest;

/**
 * A strategy for mapping update operations to existing objects.
 * 
 * @since 1.7
 */
public interface ObjectMapperFactory {

	/**
	 * Returns a mapper to handle objects of a given response.
	 */
	<T> ObjectMapper<T> forResponse(UpdateResponse<T> response);
}