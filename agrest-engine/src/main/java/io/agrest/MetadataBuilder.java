package io.agrest;

import javax.ws.rs.core.UriInfo;

/**
 * @since 1.18
 * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
@Deprecated
public interface MetadataBuilder<T> {

    MetadataBuilder<T> forResource(Class<?> resourceClass);

    MetadataBuilder<T> uri(UriInfo uriInfo);

    MetadataResponse<T> process();
}
