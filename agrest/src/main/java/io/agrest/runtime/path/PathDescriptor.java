package io.agrest.runtime.path;

import io.agrest.backend.exp.parser.ASTPath;

public interface PathDescriptor {

	boolean isAttribute();

	Class<?> getType();

	ASTPath getPathExp();
}
