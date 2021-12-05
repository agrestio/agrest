package io.agrest;

import io.agrest.access.DeleteAuthorizer;
import io.agrest.meta.AgEntityOverlay;

import java.util.Map;

/**
 * @since 1.4
 */
public interface DeleteBuilder<T> {

    DeleteBuilder<T> id(Object id);

    /**
     * @param ids multi-attribute ID
     * @since 1.20
     */
    DeleteBuilder<T> id(Map<String, Object> ids);

    /**
     * @since 2.3
     */
    DeleteBuilder<T> id(AgObjectId id);

    DeleteBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent);

    /**
     * @since 1.20
     */
    DeleteBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent);

    /**
     * Installs request-scoped {@link AgEntityOverlay} that allows to customize, add or redefine request entity structure
     * This method can be called multiple times to add more than one overlay.
     *
     * @param overlay overlay descriptor
     * @return this builder instance
     * @since 4.8
     */
    DeleteBuilder<T> entityOverlay(AgEntityOverlay<T> overlay);

    /**
     * @return this builder instance
     * @since 4.8
     */
    DeleteBuilder<T> authorizer(DeleteAuthorizer<T> authorizer);

    SimpleResponse delete();
}
