package com.nhl.link.rest.runtime.parser;

import org.apache.cayenne.exp.parser.ASTPath;

interface PathDescriptor {

	boolean isAttribute();

	String getType();

	ASTPath getPathExp();
}
