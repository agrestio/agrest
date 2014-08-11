package com.nhl.link.rest.runtime;

import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DataResponse;
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
	 * Creates a {@link SelectBuilder} to select objects related to a given root
	 * entity via specified relationship.
	 * 
	 * @since 1.2
	 */
	<T> SelectBuilder<T> forSelectRelated(Class<?> root, Object sourceId, Property<T> relationship);

	/**
	 * Creates a {@link SelectBuilder} to select objects related to a given root
	 * entity via specified relationship.
	 * 
	 * @since 1.2
	 */
	SelectBuilder<?> forSelectRelated(Class<?> root, Object sourceId, String relationship);

	/**
	 * Creates a {@link SelectBuilder} to customize data retrieval. This is the
	 * most generic and customizable way to select data. It can be used as a
	 * replacement of any other select.
	 */
	<T> SelectBuilder<T> forSelect(SelectQuery<T> query);

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
	 * Creates a relationship between source and target, creating a new target
	 * object if needed.
	 * 
	 * @since 1.2
	 */
	SimpleResponse relateNew(Class<?> sourceType, Object sourceId, String relationship, String targetData);

	/**
	 * Creates a relationship between source and target, creating a new target
	 * object if needed.
	 * 
	 * @since 1.2
	 */
	SimpleResponse relateNew(Class<?> sourceType, Object sourceId, Property<?> relationship, String targetData);

	/**
	 * Creates a relationship between existing source and target objects.
	 * 
	 * @since 1.2
	 */
	SimpleResponse relate(Class<?> sourceType, Object sourceId, String relationship, Object targetId);

	/**
	 * Creates a relationship between existing source and target objects.
	 * 
	 * @since 1.2
	 */
	SimpleResponse relate(Class<?> sourceType, Object sourceId, Property<?> relationship, Object targetId);
}
