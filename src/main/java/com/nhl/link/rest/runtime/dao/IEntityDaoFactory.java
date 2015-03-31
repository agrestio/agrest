package com.nhl.link.rest.runtime.dao;

import com.nhl.link.rest.meta.LrEntity;

/**
 * Creates DAOs for entities.
 * 
 * @since 1.15
 */
public interface IEntityDaoFactory {

	<T> EntityDao<T> dao(LrEntity<T> entity);
}
