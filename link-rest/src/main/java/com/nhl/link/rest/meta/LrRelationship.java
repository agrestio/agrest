package com.nhl.link.rest.meta;

/**
 * @since 1.12
 */
public interface LrRelationship {

	String getName();

	/**
	 * @since 2.0
	 */
	LrEntity<?> getTargetEntity();

	boolean isToMany();
}
