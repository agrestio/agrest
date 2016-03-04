package com.nhl.link.rest.meta;

/**
 * @since 1.12
 */
public interface LrRelationship {

	String getName();

	Class<?> getTargetEntityType();

	boolean isToMany();
}
