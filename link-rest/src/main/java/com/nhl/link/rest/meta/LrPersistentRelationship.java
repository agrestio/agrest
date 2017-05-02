package com.nhl.link.rest.meta;

import java.util.Collection;

/**
 * @since 1.12
 */
public interface LrPersistentRelationship extends LrRelationship {

	/**
     * @since 2.5
     */
	boolean isToDependentEntity();

	/**
     * @since 2.5
     */
	boolean isPrimaryKey();

	/**
     * @since 2.5
     */
	Collection<LrJoin> getJoins();

//	LrPersistentRelationship getReverseRelationship(); // ???
}
