package io.agrest.runtime.semantics;

import io.agrest.meta.LrEntity;
import io.agrest.meta.LrRelationship;

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
