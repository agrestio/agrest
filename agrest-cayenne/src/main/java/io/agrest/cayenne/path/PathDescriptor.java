package io.agrest.cayenne.path;

import org.apache.cayenne.exp.parser.ASTPath;

public interface PathDescriptor {

	/**
	 * @since 5.0
	 */
	boolean isAttributeOrId();

	Class<?> getType();

	ASTPath getPathExp();
}
