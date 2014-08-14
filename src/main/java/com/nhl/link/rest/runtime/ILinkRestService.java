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
	 * Creates one or more objects with a relationship from a given root object.
	 * 
	 * @since 1.3
	 */
	DataResponse<?> insertRelated(Class<?> sourceType, Object sourceId, String relationship, String targetData);

	/**
	 * Creates one or more objects with a relationship from a given root object.
	 * 
	 * @since 1.3
	 */
	<T> DataResponse<T> insertRelated(Class<?> sourceType, Object sourceId, Property<T> relationship, String targetData);

	/**
	 * Creates or updates one or more objects with a relationship from a given
	 * root object.
	 * 
	 * @since 1.3
	 * @return {@link DataResponse} containing target entity with any new
	 *         changes.
	 */
	DataResponse<?> insertOrUpdateRelated(Class<?> sourceType, Object sourceId, String relationship, String targetData);

	/**
	 * Creates or updates one or more objects with a relationship from a given
	 * root object.
	 * 
	 * @since 1.3
	 * @return {@link DataResponse} containing target entity with any new
	 *         changes.
	 */
	<T> DataResponse<T> insertOrUpdateRelated(Class<?> sourceType, Object sourceId, Property<T> relationship,
			String targetData);

	/**
	 * Creates or updates a target object with known id, and ensures there's a
	 * relationship between source and target.
	 * 
	 * @since 1.3
	 * @return {@link DataResponse} containing target entity with any new
	 *         changes.
	 */
	DataResponse<?> insertOrUpdateRelated(Class<?> sourceType, Object sourceId, String relationship, Object targetId,
			String targetData);

	/**
	 * Creates or updates a target object with known id, and ensures there's a
	 * relationship between source and target.
	 * 
	 * @since 1.3
	 * @return {@link DataResponse} containing target entity with any new
	 *         changes.
	 */
	<T> DataResponse<T> insertOrUpdateRelated(Class<?> sourceType, Object sourceId, Property<T> relationship,
			Object targetId, String targetData);

/**
	 * @deprecated since 1.3 use
	 *             {@link #insertRelated(Class, Object, String, String)
	 * @since 1.2
	 */
	@Deprecated
	DataResponse<?> relateNew(Class<?> sourceType, Object sourceId, String relationship, String targetData);

	/**
	 * @deprecated since 1.3 use
	 *             {@link #insertRelated(Class, Object, Property, String)}.
	 * @since 1.2
	 */
	@Deprecated
	<T> DataResponse<T> relateNew(Class<?> sourceType, Object sourceId, Property<T> relationship, String targetData);

	/**
	 * @deprecated since 1.3 use
	 *             {@link #insertOrUpdateRelated(Class, Object, String, Object, String)}
	 * @since 1.2
	 */
	@Deprecated
	DataResponse<?> relate(Class<?> sourceType, Object sourceId, String relationship, Object targetId, String targetData);

	/**
	 * @deprecated since 1.3 use
	 *             {@link #insertOrUpdateRelated(Class, Object, Property, Object, String)}
	 * @since 1.2
	 */
	@Deprecated
	<T> DataResponse<T> relate(Class<?> sourceType, Object sourceId, Property<T> relationship, Object targetId,
			String targetData);
}
