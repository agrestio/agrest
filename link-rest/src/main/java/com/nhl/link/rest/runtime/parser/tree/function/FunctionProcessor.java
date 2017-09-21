package com.nhl.link.rest.runtime.parser.tree.function;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.parser.PathConstants;
import com.nhl.link.rest.runtime.parser.tree.PathProcessor;
import com.nhl.link.rest.runtime.parser.tree.PathVisitor;

import javax.ws.rs.core.Response.Status;
import java.util.Objects;

public interface FunctionProcessor {

    default void processCallExpression(ResourceEntity<?> context, String expression) {
        Objects.requireNonNull(expression, "Missing expression");
        if (!expression.startsWith(PathConstants.OPEN_PARENTHESIS) || !expression.endsWith(PathConstants.CLOSE_PARENTHESIS)) {
            throw new LinkRestException(Status.BAD_REQUEST, "Expression must start and end with parentheses: " + expression);
        }
        String arguments = expression.substring(1, expression.length() - 1);
        if (arguments.isEmpty()) {
            apply(context);
        } else {
            PathVisitor visitor = new PathVisitor() {
                @Override
                public void visitAttribute(ResourceEntity<?> entity, LrAttribute attribute) {
                    apply(entity, attribute);
                }

                @Override
                public void visitRelationship(ResourceEntity<?> parent, ResourceEntity<?> child, LrRelationship relationship) {
                    apply(child);
                }

                @Override
                public void visitId(ResourceEntity<?> entity) {
                    apply(entity);
                }

                @Override
                public void visitFunction(ResourceEntity<?> context, String functionName, String callExpression) {
                    throw new LinkRestException(Status.BAD_REQUEST, "Nested functions are not allowed");
                }
            };

            PathProcessor.processor().processPath(context, arguments, visitor);
        }
    }

    void apply(ResourceEntity<?> context, LrAttribute attribute);

    void apply(ResourceEntity<?> context);
}
