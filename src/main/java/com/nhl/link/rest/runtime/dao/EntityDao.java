package com.nhl.link.rest.runtime.dao;

import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.CreateOrUpdateBuilder;
import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.SelectBuilder;

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
	CreateOrUpdateBuilder<T> update();

	/**
	 * @since 1.3
	 */
	CreateOrUpdateBuilder<T> create();

	/**
	 * @since 1.3
	 */
	CreateOrUpdateBuilder<T> createOrUpdate();

	/**
	 * @since 1.3
	 */
	CreateOrUpdateBuilder<T> idempotentCreateOrUpdate();
	
	DeleteBuilder<T> delete();

	/**
	 * @since 1.2
	 */
	void unrelate(Object sourceId, String relationship);

	/**
	 * @since 1.2
	 */
	void unrelate(Object sourceId, String relationship, Object targetId);
}
