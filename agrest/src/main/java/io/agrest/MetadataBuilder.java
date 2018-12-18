package io.agrest;

import io.agrest.constraints.Constraint;

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
    <E> MetadataBuilder<T> constraint(Constraint<T, E> constraint);

    MetadataResponse<T> process();
}
