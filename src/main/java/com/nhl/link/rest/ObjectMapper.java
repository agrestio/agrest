package com.nhl.link.rest;

/**
 * A strategy for mapping update operations to existing objects.
 * 
 * @since 1.4
 */
public interface ObjectMapper {

	/**
	 * Creates and returns a mapper tied to a specific response.
	 */
	<T> ResponseObjectMapper<T> forResponse(UpdateResponse<T> response);
}