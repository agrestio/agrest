package com.nhl.link.rest.runtime.dao;

import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateBuilder;

/**
 * A common interface of a data access object for a single entity.
 */
public interface EntityDao<T> {

	Class<T> getType();

	SelectBuilder<T> forSelect();

	SelectBuilder<T> forSelect(SelectQuery<T> query);

	/**
	 * @since 1.3
	 */
	UpdateBuilder<T> update();

	/**
	 * @since 1.3
	 */
	UpdateBuilder<T> create();

	/**
	 * @since 1.3
	 */
	UpdateBuilder<T> createOrUpdate();

	/**
	 * @since 1.3
	 */
	UpdateBuilder<T> idempotentCreateOrUpdate();

	/**
	 * @since 1.7
	 */
	UpdateBuilder<T> idempotentFullSync();

	DeleteBuilder<T> delete();

	/**
	 * @since 1.2
	 */
	SimpleResponse unrelate(Object sourceId, String relationship);

	/**
	 * @since 1.2
	 */
	SimpleResponse unrelate(Object sourceId, String relationship, Object targetId);
}
