package com.nhl.link.rest.runtime;

import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.DataResponseConfig;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;

/**
 * A facade to the ExtJS query processing pipeline.
 */
public interface ILinkRestService {

	/**
	 * Creates and returns a new {@link DataResponseConfig}. The initial state
	 * of the returned config is to include all attributes of the root entity
	 * plus the id. It can be further customized by the caller and then used
	 * repeatedly to configure DataResponses.
	 * 
	 * @since 1.1
	 */
	DataResponseConfig newConfig(Class<?> root);

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

	<T> DataResponse<T> insert(Class<T> root, String objectData);

	<T> DataResponse<T> update(Class<T> root, Object id, String objectData);

	<T> SimpleResponse delete(Class<T> root, Object id);

}
