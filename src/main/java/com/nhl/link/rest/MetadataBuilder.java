package com.nhl.link.rest;

import javax.ws.rs.core.UriInfo;

/**
 * @since 1.18
 */
public interface MetadataBuilder<T> {

	MetadataBuilder<T> forResource(Class<?> resourceClass);

	MetadataBuilder<T> uri(UriInfo uriInfo);

	MetadataResponse<T> process();
}
