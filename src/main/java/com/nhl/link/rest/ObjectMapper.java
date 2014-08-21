package com.nhl.link.rest;

/**
 * A strategy for mapping update operations to existing objects.
 * 
 * @since 1.4
 */
public interface ObjectMapper {

	<T> ResponseObjectMapper<T> forResponse(UpdateResponse<T> response);
}