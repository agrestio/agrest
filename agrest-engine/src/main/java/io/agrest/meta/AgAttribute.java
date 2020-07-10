package io.agrest.meta;

import io.agrest.property.PropertyReader;
import org.apache.cayenne.exp.parser.ASTPath;

/**
 * Represents an entity "simple" property.
 * 
 * @since 1.12
 */
public interface AgAttribute {

	String getName();

	/**
	 * @since 1.24
	 */
	Class<?> getType();

	// TODO: Cayenne API leak
	ASTPath getPathExp();

	/**
	 * @since 2.10
	 */
	PropertyReader getPropertyReader();
}
