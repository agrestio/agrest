package com.nhl.link.rest.meta;

import com.nhl.link.rest.property.PropertyReader;

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

	/**
	 * @since 2.10
	 */
	PropertyReader getPropertyReader();
}
