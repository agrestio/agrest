package com.nhl.link.rest.runtime;

import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateBuilder;

/**
 * The main entry point to LinkRest stack. Used from the user REST resource
 * classes to build request processors.
 */
public interface ILinkRestService {

	/**
	 * Selects a single object by ID.
	 */
	<T> DataResponse<T> selectById(Class<T> root, Object id);

	/**
	 * Selects a single object by ID, applying optional include/exclude
	 * information from the UriInfo to the result.
	 */
	<T> DataResponse<T> selectById(Class<T> root, Object id, UriInfo uriInfo);

	/**
	 * Selects objects based on the provided query template and extra URL
	 * parameters.
	 * 
	 * @param query
	 *            a server-side built SelectQuery that is used as a template for
	 *            database select, amended with parameters inferred from the
	 *            request.
	 * @param uriInfo
	 * @return the result of a select wrapped in a DataResponse.
	 */
	<T> DataResponse<T> select(SelectQuery<T> query, UriInfo uriInfo);

	/**
	 * Creates a {@link SelectBuilder} to customize data retrieval. This is the
	 * most generic and customizable way to select data. It can be used as a
	 * replacement of any other select.
	 * 
	 * @since 1.14
	 */
	<T> SelectBuilder<T> select(Class<T> root);

	/**
	 * Creates a {@link SelectBuilder} to customize data retrieval. This is the
	 * most generic and customizable way to select data. It can be used as a
	 * replacement of any other select.
	 * 
	 * @since 1.14
	 */
	<T> SelectBuilder<T> select(SelectQuery<T> query);

	SimpleResponse delete(Class<?> root, Object id);

	/**
	 * Breaks the relationship between source and all its target objects.
	 * 
	 * @since 1.2
	 */
	SimpleResponse unrelate(Class<?> root, Object sourceId, String relationship);

	/**
	 * Breaks the relationship between source and all its target objects.
	 * 
	 * @since 1.2
	 */
	SimpleResponse unrelate(Class<?> root, Object sourceId, Property<?> relationship);

	/**
	 * Breaks the relationship between source and a target object.
	 * 
	 * @since 1.2
	 */
	SimpleResponse unrelate(Class<?> root, Object sourceId, String relationship, Object targetId);

	/**
	 * Breaks the relationship between source and a target object.
	 * 
	 * @since 1.2
	 */
	SimpleResponse unrelate(Class<?> root, Object sourceId, Property<?> relationship, Object targetId);

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
	<T> DeleteBuilder<T> delete(Class<T> root);
}
