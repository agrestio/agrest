package com.nhl.link.rest.runtime.dao;

import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.UpdateResponse;

/**
 * A common interface of a data access object for a single entity.
 * 
 * @since 6.9
 */
public interface EntityDao<T> {

	Class<T> getType();

	SelectBuilder<T> forSelect();

	SelectBuilder<T> forSelect(SelectQuery<T> query);

	T insert(UpdateResponse<T> response);

	T update(UpdateResponse<T> response);

	void delete(Object id);
}
