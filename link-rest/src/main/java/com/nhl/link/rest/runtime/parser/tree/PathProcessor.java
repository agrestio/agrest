package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.parser.PathConstants;

import javax.ws.rs.core.Response;
import java.util.Objects;

public class PathProcessor {

	private static final PathProcessor instance = new PathProcessor();

	public static PathProcessor processor() {
		return instance;
	}

	/**
	 * Records include path, returning null for the path corresponding to an
	 * attribute, and a child {@link ResourceEntity} for the path corresponding
	 * to relationship.
	 */
    public ResourceEntity<?> processPath(ResourceEntity<?> root, String path, PathVisitor visitor) {
        ExcludeWorker.checkTooLong(path);

		int dot = path.indexOf(PathConstants.DOT);
		checkDotIndex(path, dot);

		String property = dot > 0 ? path.substring(0, dot) : path;
		LrEntity<?> lrEntity = root.getLrEntity();

		// first we must check if the path is a relationship
		LrRelationship relationship = lrEntity.getRelationship(property);
		if (relationship != null) {
			ResourceEntity<?> childEntity = root.getChildren().get(property);
			if (childEntity == null && dot > 0) {
				childEntity = root.getAggregateChildren().get(property);
			}

			if (childEntity == null) {
				LrEntity<?> targetType = relationship.getTargetEntity();
				childEntity = new ResourceEntity<>(targetType, relationship);
				root.getChildren().put(property, childEntity);
			}

			ResourceEntity<?> result;
			if (dot > 0) {
				result = processPath(childEntity, path.substring(dot + 1), visitor);
			} else {
				visitor.visitRelationship(root, childEntity, relationship);
				result = childEntity;
			}

			if (childEntity.isAggregate()) {
				root.getChildren().remove(property);
				root.getAggregateChildren().put(property, childEntity);
			}
			return result;
		}

		// if the path is not a relationship, then we must check it does not contain separators (dots)
		// TODO: this also effectively restricts attribute and function names, that contain dots,
		// but it's still possible to add attributes with such malformed names programmatically
		// Might be nice to add some additional checks, where needed
		if (dot > 0) {
			throw new LinkRestException(Response.Status.BAD_REQUEST, "Path contains dots, but is not a relationship: " + path);
		}

		LrAttribute attribute = lrEntity.getAttribute(property);
		if (attribute != null) {
			visitor.visitAttribute(root, attribute);
			return null;
		}

		// this is root entity id and it's included explicitly
		if (property.equals(PathConstants.ID_PK_ATTRIBUTE)) {
			visitor.visitId(root);
			return null;
		}

		String functionName = readFunctionName(property);
		if (functionName != null) {
			visitor.visitFunction(root, functionName, property.substring(functionName.length()));
			return null;
		}

		throw new LinkRestException(Response.Status.BAD_REQUEST, "Invalid include path: " + path);
    }

	private static void checkDotIndex(String path, int dot) {
		if (dot == 0) {
			throw new LinkRestException(Response.Status.BAD_REQUEST, "Path starts with dot: " + path);
		}
		if (dot == path.length() - 1) {
			throw new LinkRestException(Response.Status.BAD_REQUEST, "Path ends with dot: " + path);
		}
	}

	/**
	 * @param property Function call expression (e.g. {@code count()})
	 * @return Function name or null, if {@code property} is not a function call expression
     */
	private static String readFunctionName(String property) {
		if (Objects.requireNonNull(property, "Missing property").isEmpty()) {
			return null;
		}

		int parenthesis = property.indexOf(PathConstants.OPEN_PARENTHESIS);
		if (parenthesis == 0) {
			throw new LinkRestException(Response.Status.BAD_REQUEST,
					"Function call expression starts with parenthesis (missing function name): " + property);
		} else if (parenthesis < 0) {
			return null;
		}

		return property.substring(0, parenthesis);
	}
}
