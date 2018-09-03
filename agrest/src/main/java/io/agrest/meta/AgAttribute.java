package io.agrest.meta;

import io.agrest.property.PropertyReader;
import org.apache.cayenne.exp.parser.ASTPath;

/**
 * Represents an entity "simple" property.
 * 
 * @since 1.12
 */
public interface AgAttribute {

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
	ASTPath getPathExp();

	/**
	 * @since 2.10
	 */
	PropertyReader getPropertyReader();
}
