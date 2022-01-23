package io.agrest.meta.parser;

import io.agrest.meta.AgResource;

import java.util.Collection;

/**
 * @since 1.18
 * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
// NOTE: this interface is implemented in "bootique-jaxrs", as "agrest-engine" has no access to JAX-RS code and can't
// parse JAX-RS annotations.
@Deprecated
public interface IResourceParser {

	<T> Collection<AgResource<?>> parse(Class<T> resourceClass);

}
