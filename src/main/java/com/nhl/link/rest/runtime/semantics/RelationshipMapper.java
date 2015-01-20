package com.nhl.link.rest.runtime.semantics;

import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;

public class RelationshipMapper implements IRelationshipMapper {

	@Override
	public String toRelatedIdName(LrRelationship relationship) {
		return relationship.getName();
	}

	@Override
	public LrRelationship toRelationship(LrEntity<?> root, String relatedIdName) {
		return root.getRelationship(relatedIdName);
	}

}
