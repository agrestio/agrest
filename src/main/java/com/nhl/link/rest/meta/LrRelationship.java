package com.nhl.link.rest.meta;

import org.apache.cayenne.map.ObjRelationship;

/**
 * @since 1.12
 */
public interface LrRelationship {

	String getName();

	LrEntity<?> getTargetEntity();

	boolean isToMany();

	ObjRelationship getObjRelationship();
}
