package io.agrest.meta;

import io.agrest.annotation.LinkType;

import java.util.Collection;

/**
 * @param <T> The type of the resource entity model.
 * @since 1.18
 * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
@Deprecated
public interface AgResource<T> {

    String getPath();

    LinkType getType();

    Collection<AgOperation> getOperations();

    AgEntity<T> getEntity();
}
