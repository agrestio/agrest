package com.nhl.link.rest;

import javax.ws.rs.core.UriInfo;

public interface MetadataBuilder<T> {

    MetadataBuilder<T> forResource(Class<?> resourceClass);
    MetadataBuilder<T> uri(UriInfo uriInfo);
    MetadataBuilder<T> path(String path);
    MetadataResponse process();

}
