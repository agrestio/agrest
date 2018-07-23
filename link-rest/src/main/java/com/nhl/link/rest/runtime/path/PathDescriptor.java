package com.nhl.link.rest.runtime.path;

import org.apache.cayenne.exp.parser.ASTPath;

public interface PathDescriptor {

	boolean isAttribute();

	Class<?> getType();

	ASTPath getPathExp();
}
