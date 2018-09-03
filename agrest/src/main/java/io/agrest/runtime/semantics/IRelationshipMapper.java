package io.agrest.runtime.semantics;

import io.agrest.meta.LrEntity;
import io.agrest.meta.LrRelationship;

public interface IRelationshipMapper {

	LrRelationship toRelationship(LrEntity<?> root, String relatedIdName);

	String toRelatedIdName(LrRelationship relationship);
}
