package com.nhl.link.rest.meta;

import org.apache.cayenne.map.ObjRelationship;

/**
 * @since 1.12
 */
public interface LrPersistentRelationship extends LrRelationship {

	ObjRelationship getObjRelationship();
}
