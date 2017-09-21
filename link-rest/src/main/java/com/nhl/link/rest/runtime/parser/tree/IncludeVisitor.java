package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrRelationship;

import javax.ws.rs.core.Response.Status;

public class IncludeVisitor implements PathVisitor {

    private static final IncludeVisitor instance = new IncludeVisitor();

    public static IncludeVisitor visitor() {
        return instance;
    }

    @Override
    public void visitAttribute(ResourceEntity<?> entity, LrAttribute attribute) {
        entity.getAttributes().put(attribute.getName(), attribute);
    }

    @Override
    public void visitRelationship(ResourceEntity<?> parent, ResourceEntity<?> child, LrRelationship relationship) {
        IncludeWorker.applyDefaultIncludes(child);
        // Id should be included implicitly
        child.includeId();
    }

    @Override
    public void visitId(ResourceEntity<?> entity) {
        entity.includeId();
    }

    @Override
    public void visitFunction(ResourceEntity<?> context, String functionName, String callExpression) {
        throw new LinkRestException(Status.BAD_REQUEST, "Functions are not allowed in the current context");
    }
}
