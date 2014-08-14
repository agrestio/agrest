package com.nhl.link.rest.runtime.dao;

import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.UpdateResponse;

/**
 * A common interface of a data access object for a single entity.
 */
public interface EntityDao<T> {

	Class<T> getType();

	SelectBuilder<T> forSelect();

	SelectBuilder<T> forSelect(SelectQuery<T> query);

	T insert(UpdateResponse<T> response);

	T update(UpdateResponse<T> response);

	void delete(Object id);

	/**
	 * @since 1.2
	 */
	void unrelate(Object sourceId, String relationship);

	/**
	 * @since 1.2
	 */
	void unrelate(Object sourceId, String relationship, Object targetId);

	/**
	 * @deprecated unused since 1.3
	 * @since 1.2
	 */
	@Deprecated
	<A> A relate(Object sourceId, String relationship, UpdateResponse<A> targetData);

	/**
	 * @since 1.3
	 */
	void insertOrUpdateRelated(Object sourceId, String relationship, UpdateResponse<?> targetData);
}
