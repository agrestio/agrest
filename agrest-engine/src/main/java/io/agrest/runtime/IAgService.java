package io.agrest.runtime;

import io.agrest.DeleteBuilder;
import io.agrest.EntityDelete;
import io.agrest.MetadataBuilder;
import io.agrest.SelectBuilder;
import io.agrest.SimpleResponse;
import io.agrest.UnrelateBuilder;
import io.agrest.UpdateBuilder;

import java.util.Collection;

/**
 * The main entry point to Agrest stack. Indirectly used from the user REST resource classes to build request processors.
 */
public interface IAgService {

    /**
     * Creates a {@link SelectBuilder} to customize data retrieval.
     *
     * @since 1.14
     */
    <T> SelectBuilder<T> select(Class<T> type);

    /**
     * @since 2.3
     * @deprecated since 5.0 as DELETE HTTP method has no body. Can be replaced with "delete(Class).byId(id1).byId(id2)"
     */
    @Deprecated
    <T> SimpleResponse delete(Class<T> type, Collection<EntityDelete<T>> deleted);

    /**
     * Creates a {@link UnrelateBuilder} to build an operation breaking a relationship between a source objects and
     * some or all related objects.
     *
     * @since 5.0
     */
    <T> UnrelateBuilder<T> unrelate(Class<T> type);

    /**
     * Breaks the relationship between source and all its target objects.
     *
     * @since 1.2
     * @deprecated since 5.0 in favor of a builder created per {@link #unrelate(Class)}.
     */
    @Deprecated
    default <T> SimpleResponse unrelate(Class<T> type, Object sourceId, String relationship) {
        return unrelate(type).sourceId(sourceId).allRelated(relationship).sync();
    }

    /**
     * Breaks the relationship between source and a target object.
     *
     * @since 1.2
     * @deprecated since 5.0 in favor of a builder created per {@link #unrelate(Class)}.
     */
    @Deprecated
    default <T> SimpleResponse unrelate(Class<T> type, Object sourceId, String relationship, Object targetId) {
        return unrelate(type).sourceId(sourceId).related(relationship, targetId).sync();
    }

    /**
     * @since 1.3
     */
    <T> UpdateBuilder<T> update(Class<T> type);

    /**
     * @since 1.3
     */
    <T> UpdateBuilder<T> create(Class<T> type);

    /**
     * @since 1.3
     */
    <T> UpdateBuilder<T> createOrUpdate(Class<T> type);

    /**
     * Returns an UpdateBuilder that would perform an idempotent
     * create-or-update operation on the request objects. The operation will
     * fail if it can't be executed as idempotent. The condition is usually that
     * all object's ID should be passed explicitly in request or can be implied
     * from a relationship. Otherwise the server will have no way of mapping
     * update data to an existing object and the update can't be idempotent.
     *
     * @since 1.3
     */
    <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type);

    /**
     * Returns an UpdateBuilder that would perform create/update/delete
     * operations as needed to synchronize backend data with the state of the
     * request collection. The operation will fail if it can't be executed as
     * idempotent. The condition is usually that all object's ID should be
     * passed explicitly in request or can be implied from a relationship.
     * Otherwise the server will have no way of mapping update data to an
     * existing object and the update can't be idempotent.
     *
     * @since 1.7
     */
    <T> UpdateBuilder<T> idempotentFullSync(Class<T> type);

    /**
     * @since 1.4
     */
    <T> DeleteBuilder<T> delete(Class<T> type);

    /**
     * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
     */
    @Deprecated
    <T> MetadataBuilder<T> metadata(Class<T> type);
}
