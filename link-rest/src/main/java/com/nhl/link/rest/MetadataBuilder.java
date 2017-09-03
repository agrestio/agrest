package com.nhl.link.rest;

import com.nhl.link.rest.constraints.Constraint;

import javax.ws.rs.core.UriInfo;

/**
 * @since 1.18
 */
public interface MetadataBuilder<T> {

    MetadataBuilder<T> forResource(Class<?> resourceClass);

    MetadataBuilder<T> uri(UriInfo uriInfo);

    /**
     * Installs an optional constraint function defining which attributes / relationships the client can see.
     *
     * @param constraint Constraint function.
     * @return this builder instance.
     * @since 2.10
     */
    MetadataBuilder<T> constraint(Constraint<T> constraint);

    MetadataResponse<T> process();
}
