package com.nhl.link.rest.runtime.semantics;

import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;

public class RelationshipMapper implements IRelationshipMapper {

	@Override
	public String toRelatedIdName(ObjRelationship relationship) {
		return relationship.getName();
	}

	@Override
	public ObjRelationship toRelationship(ObjEntity root, String relatedIdName) {
		return root.getRelationship(relatedIdName);
	}

}
