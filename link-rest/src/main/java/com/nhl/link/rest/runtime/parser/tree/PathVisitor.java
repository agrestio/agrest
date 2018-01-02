package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrRelationship;

public interface PathVisitor {

    void visitAttribute(ResourceEntity<?> entity, LrAttribute attribute);

	void visitRelationship(ResourceEntity<?> parent, ResourceEntity<?> child, LrRelationship relationship);

	void visitId(ResourceEntity<?> entity);

    void visitFunction(ResourceEntity<?> context, String functionName, String callExpression);
}
