package com.nhl.link.rest.runtime.parser.tree;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.PathConstants;

class ExcludeWorker {

	private IJacksonService jsonParser;

	ExcludeWorker(IJacksonService jsonParser) {
		this.jsonParser = jsonParser;
	}

	/**
	 * Sanity check. We don't want to get a stack overflow.
	 */
	static void checkTooLong(String path) {
		if (path != null && path.length() > PathConstants.MAX_PATH_LENGTH) {
			throw new LinkRestException(Status.BAD_REQUEST, "Include/exclude path too long: " + path);
		}
	}

	void process(ResourceEntity<?> resourceEntity, List<String> excludes) {
		for (String exclude : excludes) {
			if (exclude.startsWith("[")) {
				processExcludeArray(resourceEntity, exclude);
			} else {
				processExcludePath(resourceEntity, exclude);
			}
		}
	}

	private void processExcludeArray(ResourceEntity<?> resourceEntity, String exclude) {
		JsonNode root = jsonParser.parseJson(exclude);

		if (root != null && root.isArray()) {

			for (JsonNode child : root) {
				if (child.isTextual()) {
					processExcludePath(resourceEntity, child.asText());
				} else {
					throw new LinkRestException(Status.BAD_REQUEST, "Bad exclude spec: " + child);
				}
			}
		}
	}

	void processExcludePath(ResourceEntity<?> resourceEntity, String path) {

		checkTooLong(path);
		int dot = path.indexOf(PathConstants.DOT);

		if (dot == 0) {
			throw new LinkRestException(Status.BAD_REQUEST, "Exclude starts with dot: " + path);
		}

		if (dot == path.length() - 1) {
			throw new LinkRestException(Status.BAD_REQUEST, "Exclude ends with dot: " + path);
		}

		String property = dot > 0 ? path.substring(0, dot) : path;
		if (resourceEntity.getLrEntity().getPersistentAttribute(property) != null) {

			if (dot > 0) {
				throw new LinkRestException(Status.BAD_REQUEST, "Invalid exclude path: " + path);
			}

			resourceEntity.getAttributes().remove(property);
			return;
		}

		if (resourceEntity.getLrEntity().getRelationship(property) != null) {

			ResourceEntity<?> relatedFilter = resourceEntity.getChild(property);
			if (relatedFilter == null) {
				// valid path, but not included... ignoring
				return;
			}

			if (dot > 0) {
				processExcludePath(relatedFilter, path.substring(dot + 1));
			}
			return;
		}

		// this is an entity id and it's excluded explicitly
		if (property.equals(PathConstants.ID_PK_ATTRIBUTE)) {
			resourceEntity.excludeId();
			return;
		}

		throw new LinkRestException(Status.BAD_REQUEST, "Invalid exclude path: " + path);
	}

}
