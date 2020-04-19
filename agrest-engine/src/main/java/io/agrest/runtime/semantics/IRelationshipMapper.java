package io.agrest.runtime.semantics;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;

public interface IRelationshipMapper {

	AgRelationship toRelationship(AgEntity<?> root, String relatedIdName);

	String toRelatedIdName(AgRelationship relationship);
}
