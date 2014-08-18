package com.nhl.link.rest.runtime.parser;

import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.UpdateResponse;

/**
 * Defines protocol adapter between the REST interface and LinkRest backend.
 */
public interface IRequestParser {

	<T> DataResponse<T> parseSelect(DataResponse<T> response, UriInfo uriInfo, String autocompleteProperty);

	/**
	 * Parses an update that may contain zero or more objects of a single kind
	 * with or without IDs.
	 * 
	 * @since 1.3
	 */
	<T> UpdateResponse<T> parseUpdate(UpdateResponse<T> response, String entityData);

}
