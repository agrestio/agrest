package io.agrest.runtime.semantics;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;

public class RelationshipMapper implements IRelationshipMapper {

	@Override
	public String toRelatedIdName(AgRelationship relationship) {
		return relationship.getName();
	}

	@Override
	public AgRelationship toRelationship(AgEntity<?> root, String relatedIdName) {
		return root.getRelationship(relatedIdName);
	}

}
