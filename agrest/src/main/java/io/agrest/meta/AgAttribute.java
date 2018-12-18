package io.agrest.meta;

import io.agrest.property.PropertyReader;

/**
 * Represents an entity "simple" property.
 * 
 * @since 1.12
 */
public interface AgAttribute<P> {

	/**
	 * @since 1.12
     */
	String getName();

	/**
	 * @since 1.24
	 */
	Class<?> getType();

	/**
	 * @since 1.12
     */
	P getPathExp();

	/**
	 * @since 2.10
	 */
	PropertyReader getPropertyReader();
}
