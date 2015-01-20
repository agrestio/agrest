package com.nhl.link.rest.meta;

import org.apache.cayenne.map.ObjAttribute;

/**
 * Represents a persistent attribute.
 * 
 * @since 1.12
 */
public interface LrAttribute {
	
	String getName();

	String getJavaType();

	int getJdbcType();
	
	ObjAttribute getObjAttribute();
}
