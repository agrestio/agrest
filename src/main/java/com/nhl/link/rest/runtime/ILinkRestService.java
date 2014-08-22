package com.nhl.link.rest.runtime;

import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.CreateOrUpdateBuilder;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;

/**
 * An entry point to LinkRest backend services. Used from the user REST resource
 * classes to process requests.
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
	 */
	<T> SelectBuilder<T> forSelect(Class<T> root);

	/**
	 * Creates a {@link SelectBuilder} to customize data retrieval. This is the
	 * most generic and customizable way to select data. It can be used as a
	 * replacement of any other select.
	 */
	<T> SelectBuilder<T> forSelect(SelectQuery<T> query);

	/**
	 * @deprecated since 1.3 use {@link #create(Class)}.
	 */
	<T> DataResponse<T> insert(Class<T> root, String objectData);

	<T> DataResponse<T> update(Class<T> root, Object id, String objectData);

	SimpleResponse delete(Class<?> root, Object id);

	/**
	 * Breaks the relationship between source and all its target objects. If a
	 * target object can't exist without source, the target object itself is
	 * deleted.
	 * 
	 * @since 1.2
	 */
	SimpleResponse unrelate(Class<?> root, Object sourceId, String relationship);

	/**
	 * Breaks the relationship between source and all its target objects. If a
	 * target object can't exist without source, the target object itself is
	 * deleted.
	 * 
	 * @since 1.2
	 */
	SimpleResponse unrelate(Class<?> root, Object sourceId, Property<?> relationship);

	/**
	 * Breaks the relationship between source and a target object. If the target
	 * object can't exist without source, the target object itself is deleted.
	 * 
	 * @since 1.2
	 */
	SimpleResponse unrelate(Class<?> root, Object sourceId, String relationship, Object targetId);

	/**
	 * Breaks the relationship between source and a target object. If the target
	 * object can't exist without source, the target object itself is deleted.
	 * 
	 * @since 1.2
	 */
	SimpleResponse unrelate(Class<?> root, Object sourceId, Property<?> relationship, Object targetId);

	/**
	 * @since 1.3
	 */
	<T> CreateOrUpdateBuilder<T> update(Class<T> type);

	/**
	 * @since 1.3
	 */
	<T> CreateOrUpdateBuilder<T> create(Class<T> type);

	/**
	 * @since 1.3
	 */
	<T> CreateOrUpdateBuilder<T> createOrUpdate(Class<T> type);

	/**
	 * 
	 * Returns a CreateOrUpdateBuilder that would perform an idempotent
	 * create-or-update operation on the request objects. The operation will
	 * fail if it can't be executed as idempotent. The condition is usually that
	 * all object's ID should be passed explicitly in request or can be implied
	 * from a relationship. Otherwise the server will have no way of mapping
	 * update data to an existing object and the update can't be idempotent.
	 * 
	 * @since 1.3
	 */
	<T> CreateOrUpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type);

	/**
	 * @since 1.4
	 */
	<T> DeleteBuilder<T> delete(Class<T> root);
}
