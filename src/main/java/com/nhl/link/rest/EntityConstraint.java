package com.nhl.link.rest;

/**
 * @since 1.6
 */
public interface EntityConstraint {

	String getEntityName();

	boolean allowsId();

	boolean allowsAllAttributes();

	boolean allowsAttribute(String name);

	boolean allowsRelationship(String name);

}
