package com.nhl.link.rest.runtime.parser;

import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;

/**
 * Defines protocol adapter between the REST interface and LinkRest backend.
 */
public interface IRequestParser {

	/**
	 * Parses request control parameters, creating a {@link ResourceEntity},
	 * representing client request.
	 * 
	 * @since 1.20
	 */
	<T> ResourceEntity<T> parseSelect(LrEntity<T> entity, UriInfo uriInfo, String autocompleteProperty);

	/**
	 * Parses request control parameters, creating a {@link ResourceEntity},
	 * representing updating client request.
	 * 
	 * @since 1.20
	 */
	<T> ResourceEntity<T> parseUpdate(LrEntity<T> entity, UriInfo uriInfo);

}
