package io.agrest.runtime;

import io.agrest.*;

import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Map;

/**
 * The main entry point to Agrest stack. Indirectly used from the user REST resource classes to build request processors.
 */
public interface IAgService {

    /**
     * Selects a single object by ID.
     *
     * @deprecated since 4.1 use "select(type).byId(id).get()"
     */
    @Deprecated
    default <T> DataResponse<T> selectById(Class<T> type, Object id) {
        return select(type).byId(id).get();
    }

    /**
     * Selects a single object by ID, applying optional include/exclude
     * information from the UriInfo to the result.
     *
     * @deprecated since 4.1 use "select(type).uri(uriInfo).byId(id).get()"
     */
    @Deprecated
    default <T> DataResponse<T> selectById(Class<T> type, Object id, UriInfo uriInfo) {
        return select(type).uri(uriInfo).byId(id).get();
    }

    /**
     * Creates a {@link SelectBuilder} to customize data retrieval. This is the
     * most generic and customizable way to select data. It can be used as a
     * replacement of any other select.
     *
     * @since 1.14
     */
    <T> SelectBuilder<T> select(Class<T> type);

    /**
     * @deprecated since 4.1 use "delete(type).id(id).delete()"
     */
    default SimpleResponse delete(Class<?> type, Object id) {
        return delete(type).id(id).sync();
    }

    /**
     * @deprecated since 4.1 use "delete(type).id(ids).delete()"
     */
    @Deprecated
    default SimpleResponse delete(Class<?> type, Map<String, Object> ids) {
        return delete(type).id(ids).sync();
    }

    /**
     * @since 2.3
     */
    // TODO: move this to Sencha.. Base Agrest should not support bulk deletes, as DELETE HTTP method has no body
    <T> SimpleResponse delete(Class<T> type, Collection<EntityDelete<T>> deleted);

    /**
     * Breaks the relationship between source and all its target objects.
     *
     * @since 1.2
     */
    <T> SimpleResponse unrelate(Class<T> type, Object sourceId, String relationship);

    /**
     * Breaks the relationship between source and a target object.
     *
     * @since 1.2
     */
    <T> SimpleResponse unrelate(Class<T> type, Object sourceId, String relationship, Object targetId);

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
