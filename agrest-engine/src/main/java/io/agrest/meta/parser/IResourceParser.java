package io.agrest.meta.parser;

import io.agrest.meta.AgResource;

import java.util.Collection;

/**
 * @since 1.18
 * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
@Deprecated
public interface IResourceParser {

	<T> Collection<AgResource<?>> parse(Class<T> resourceClass);

}
