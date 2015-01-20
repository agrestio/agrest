package com.nhl.link.rest.runtime.semantics;

import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;

public interface IRelationshipMapper {

	LrRelationship toRelationship(LrEntity<?> root, String relatedIdName);

	String toRelatedIdName(LrRelationship relationship);
}
