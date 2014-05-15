package com.nhl.link.rest.runtime.semantics;

import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;

public interface IRelationshipMapper {

	ObjRelationship toRelationship(ObjEntity root, String relatedIdName);

	String toRelatedIdName(ObjRelationship relationship);
}
