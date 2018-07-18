package com.nhl.link.rest.runtime.parser;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @since 1.5
 */
public abstract class BaseRequestProcessor {

	public static String string(Map<String, List<String>> parameters, String name) {

		List<String> strings = strings(parameters, name);
		return strings.isEmpty() ? null : strings.get(0);
	}

    public static List<String> strings(Map<String, List<String>> parameters, String name) {
		if (parameters == null) {
			return 	Collections.emptyList();
		}

		List<String> result = parameters.get(name);
		if (result == null) {
			result = Collections.emptyList();
		}

		return result;
	}

    public static int integer(Map<String, List<String>> parameters, String name) {

		List<String> strings = strings(parameters, name);
		String value =  strings.isEmpty() ? null : strings.get(0);

		if (value == null) {
			return -1;
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfex) {
			return -1;
		}
	}

	/**
	 * Sanity check. We don't want to get a stack overflow.
	 */
	public static void checkTooLong(String path) {
		if (path != null && path.length() > PathConstants.MAX_PATH_LENGTH) {
			throw new LinkRestException(Response.Status.BAD_REQUEST, "Include/exclude path too long: " + path);
		}
	}

	/**
	 * Records include path, returning null for the path corresponding to an
	 * attribute, and a child {@link ResourceEntity} for the path corresponding
	 * to relationship.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ResourceEntity<?> processIncludePath(ResourceEntity<?> parent, String path) {
		int dot = path.indexOf(PathConstants.DOT);

		if (dot == 0) {
			throw new LinkRestException(Status.BAD_REQUEST, "Include starts with dot: " + path);
		}

		if (dot == path.length() - 1) {
			throw new LinkRestException(Status.BAD_REQUEST, "Include ends with dot: " + path);
		}

		String property = dot > 0 ? path.substring(0, dot) : path;
		LrEntity<?> lrEntity = parent.getLrEntity();
		LrAttribute attribute = lrEntity.getAttribute(property);
		if (attribute != null) {

			if (dot > 0) {
				throw new LinkRestException(Status.BAD_REQUEST, "Invalid include path: " + path);
			}

			parent.getAttributes().put(property, attribute);
			return null;
		}

		LrRelationship relationship = lrEntity.getRelationship(property);
		if (relationship != null) {

			ResourceEntity<?> childEntity = parent.getChild(property);
			if (childEntity == null) {
				LrEntity<?> targetType = relationship.getTargetEntity();
				childEntity = new ResourceEntity(targetType, relationship);
				parent.getChildren().put(property, childEntity);
			}

			if (dot > 0) {
				return processIncludePath(childEntity, path.substring(dot + 1));
			} else {
				processDefaultIncludes(childEntity);
				// Id should be included implicitly
				childEntity.includeId();
				return childEntity;
			}
		}

		// this is root entity id and it's included explicitly
		if (property.equals(PathConstants.ID_PK_ATTRIBUTE)) {
			parent.includeId();
			return null;
		}

		throw new LinkRestException(Status.BAD_REQUEST, "Invalid include path: " + path);
	}

	public static void processDefaultIncludes(ResourceEntity<?> resourceEntity) {
		// either there are no includes (taking into account Id) or all includes
		// are relationships
		if (!resourceEntity.isIdIncluded() && resourceEntity.getAttributes().isEmpty()) {

			for (LrAttribute a : resourceEntity.getLrEntity().getAttributes()) {
				resourceEntity.getAttributes().put(a.getName(), a);
				resourceEntity.getDefaultProperties().add(a.getName());
			}

			// Id should be included by default
			resourceEntity.includeId();
		}
	}
}
