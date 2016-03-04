package com.nhl.link.rest.meta;

import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.ObjAttribute;

/**
 * Represents a persistent attribute.
 * 
 * @since 1.12
 */
public interface LrPersistentAttribute extends LrAttribute {

	int getJdbcType();

	ObjAttribute getObjAttribute();
	
	DbAttribute getDbAttribute();
}
