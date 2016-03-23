package com.nhl.link.rest.runtime.meta;

import org.apache.cayenne.query.Select;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;

/**
 * Provides access to LinkRest entity metadata.
 */
public interface IMetadataService {

	/**
	 * @since 1.12
	 */
	<T> LrEntity<T> getLrEntity(Class<T> type);

	/**
	 * @since 1.12
	 */
	<T> LrEntity<T> getLrEntity(Select<T> query);

	/**
	 * Returns a named relationship for a given object type. If the type is not
	 * supported or there is no matching relationship, an exception is thrown.
	 * 
	 * @since 1.12
	 */
	LrRelationship getLrRelationship(Class<?> type, String relationship);

	/**
	 * Returns a relationship to child for a given {@link EntityParent}. If the
	 * type is not supported or there is no matching relationship, an exception
	 * is thrown.
	 * 
	 * @since 1.12
	 */
	LrRelationship getLrRelationship(EntityParent<?> parent);
}
