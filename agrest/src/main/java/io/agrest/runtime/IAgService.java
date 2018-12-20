package io.agrest.runtime;

import io.agrest.DataResponse;
import io.agrest.DeleteBuilder;
import io.agrest.EntityDelete;
import io.agrest.MetadataBuilder;
import io.agrest.SelectBuilder;
import io.agrest.SimpleResponse;
import io.agrest.UpdateBuilder;

import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Map;

/**
 * The main entry point to Agrest stack. Used from the user REST resource
 * classes to build request processors.
 */
public interface IAgService {

	/**
	 * Selects a single object by ID.
	 */
	<T> DataResponse<T> selectById(Class<T> type, Object id);

	/**
	 * Selects a single object by ID, applying optional include/exclude
	 * information from the UriInfo to the result.
	 */
	<T> DataResponse<T> selectById(Class<T> type, Object id, UriInfo uriInfo);

	/**
	 * Creates a {@link SelectBuilder} to customize data retrieval. This is the
	 * most generic and customizable way to select data. It can be used as a
	 * replacement of any other select.
	 * 
	 * @since 1.14
	 */
	<T, E> SelectBuilder<T, E> select(Class<T> type);

	<T, E> SelectBuilder<T, E> select(Class<T> type, Class<E> expression);

	SimpleResponse delete(Class<?> type, Object id);

	SimpleResponse delete(Class<?> type, Map<String, Object> ids);

	/**
	 * @since 2.3
     */
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
	<T, E> UpdateBuilder<T, E> update(Class<T> type);

	/**
	 * @since 1.3
	 */
	<T, E> UpdateBuilder<T, E> create(Class<T> type);

	/**
	 * @since 1.3
	 */
	<T, E> UpdateBuilder<T, E> createOrUpdate(Class<T> type);

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
	<T, E> UpdateBuilder<T, E> idempotentCreateOrUpdate(Class<T> type);

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
	<T, E> UpdateBuilder<T, E> idempotentFullSync(Class<T> type);

	/**
	 * @since 1.4
	 */
	<T> DeleteBuilder<T> delete(Class<T> type);

	<T> MetadataBuilder<T> metadata(Class<T> type);
}
