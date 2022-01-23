package io.agrest.meta.parser;

import io.agrest.meta.AgResource;

import java.util.Collection;

/**
 * @since 5.0
 * @deprecated since 5.0, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
@Deprecated
public class ThrowingResourceParser implements IResourceParser {

    @Override
    public <T> Collection<AgResource<?>> parse(Class<T> resourceClass) {
        throw new UnsupportedOperationException("Add 'agrest-jaxrs' module for a working implementation of IResourceParser");
    }
}
