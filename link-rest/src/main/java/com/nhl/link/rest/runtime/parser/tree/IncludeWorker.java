package com.nhl.link.rest.runtime.parser.tree;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.PathConstants;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import org.apache.cayenne.exp.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import java.util.List;

public class IncludeWorker {

	private static final Logger LOGGER = LoggerFactory.getLogger(IncludeWorker.class);

	private static final String PATH = "path";
	private static final String MAP_BY = "mapBy";
	private static final String SORT = "sort";
	private static final String CAYENNE_EXP = "cayenneExp";
	private static final String START = "start";
	private static final String LIMIT = "limit";

	private IJacksonService jsonParser;
	private ISortProcessor sortProcessor;
	private ICayenneExpProcessor expProcessor;

	public IncludeWorker(IJacksonService jsonParser, ISortProcessor sortProcessor, ICayenneExpProcessor expProcessor) {
		this.jsonParser = jsonParser;
		this.sortProcessor = sortProcessor;
		this.expProcessor = expProcessor;
	}

	public void process(ResourceEntity<?> resourceEntity, List<String> includes) {
		for (String include : includes) {

			if (include.startsWith("[")) {
				processIncludeArray(resourceEntity, include);
			} else if (include.startsWith("{")) {
				JsonNode root = jsonParser.parseJson(include);
				processIncludeObject(resourceEntity, root);
			} else {
				processIncludePath(resourceEntity, include);
			}
		}

		processDefaultIncludes(resourceEntity);
	}

	private void processIncludeArray(ResourceEntity<?> resourceEntity, String include) {
		JsonNode root = jsonParser.parseJson(include);

		if (root != null && root.isArray()) {

			for (JsonNode child : root) {

				if (child.isObject()) {
					processIncludeObject(resourceEntity, child);
				} else if (child.isTextual()) {
					processIncludePath(resourceEntity, child.asText());
				} else {
					throw new LinkRestException(Status.BAD_REQUEST, "Bad include spec: " + child);
				}
			}
		}
	}

	private void processIncludeObject(ResourceEntity<?> rootEntity, JsonNode root) {

		if (root != null) {

			ResourceEntity<?> includeEntity;

			JsonNode pathNode = root.get(PATH);
			if (pathNode == null || !pathNode.isTextual()) {
				// root node
				includeEntity = rootEntity;
			} else {
				String path = pathNode.asText();
				includeEntity = processIncludePath(rootEntity, path);
				if (includeEntity == null) {
					throw new LinkRestException(Status.BAD_REQUEST,
							"Bad include spec, non-relationship 'path' in include object: " + path);
				}
			}

			JsonNode mapByNode = root.get(MAP_BY);
			if (mapByNode != null) {
				if (!mapByNode.isTextual()) {
					throw new LinkRestException(Status.BAD_REQUEST, "Bad include spec - invalid 'mapBy': " + root);
				}

				processMapBy(includeEntity, mapByNode.asText());
			}

			JsonNode sortNode = root.get(SORT);
			if (sortNode != null) {
				sortProcessor.process(includeEntity, sortNode);
			}

			JsonNode expNode = root.get(CAYENNE_EXP);
			if (expNode != null) {
				Expression exp = expProcessor.process(includeEntity.getLrEntity(), expNode);
				if (exp != null) {
					includeEntity.andQualifier(exp);
				}
			}

			JsonNode startNode = root.get(START);
			if (startNode != null) {
				includeEntity.setFetchOffset(startNode.asInt());
			}

			JsonNode limitNode = root.get(LIMIT);
			if (limitNode != null) {
				includeEntity.setFetchLimit(limitNode.asInt());
			}
		}
	}

	private <T> void processMapBy(ResourceEntity<T> descriptor, String mapByPath) {

		if (descriptor == null) {
			LOGGER.info("Ignoring 'mapBy:" + mapByPath + "' for non-relationship property");
			return;
		}

		// either root list, or to-many relationship
		if (descriptor.getIncoming() == null || descriptor.getIncoming().isToMany()) {

			ResourceEntity<T> mapByRoot = new ResourceEntity<T>(descriptor.getLrEntity());
			processIncludePath(mapByRoot, mapByPath);
			descriptor.mapBy(mapByRoot, mapByPath);

		} else {
			LOGGER.info("Ignoring 'mapBy:" + mapByPath + "' for to-one relationship property");
		}
	}

	/**
	 * Records include path, returning null for the path corresponding to an
	 * attribute, and a child {@link ResourceEntity} for the path corresponding
	 * to relationship.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ResourceEntity<?> processIncludePath(ResourceEntity<?> parent, String path) {

		ExcludeWorker.checkTooLong(path);

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

	private void processDefaultIncludes(ResourceEntity<?> resourceEntity) {
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
