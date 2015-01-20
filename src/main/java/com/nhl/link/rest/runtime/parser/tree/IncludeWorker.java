package com.nhl.link.rest.runtime.parser.tree;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.PathConstants;
import com.nhl.link.rest.runtime.parser.filter.IFilterProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;

class IncludeWorker {

	private static final Logger LOGGER = LoggerFactory.getLogger(IncludeWorker.class);

	private static final String PATH = "path";
	private static final String MAP_BY = "mapBy";
	private static final String SORT = "sort";
	private static final String CAYENNE_EXP = "cayenneExp";

	private IJacksonService jsonParser;
	private ISortProcessor sortProcessor;
	private IFilterProcessor expProcessor;
	private IMetadataService metadataService;

	IncludeWorker(IJacksonService jsonParser, ISortProcessor sortProcessor, IFilterProcessor expProcessor,
			IMetadataService metadataService) {
		this.jsonParser = jsonParser;
		this.sortProcessor = sortProcessor;
		this.expProcessor = expProcessor;
		this.metadataService = metadataService;
	}

	void process(ResourceEntity<?> clientEntity, List<String> includes) {
		for (String include : includes) {

			if (include.startsWith("[")) {
				processIncludeArray(clientEntity, include);
			} else if (include.startsWith("{")) {
				JsonNode root = jsonParser.parseJson(include);
				processIncludeObject(clientEntity, root);
			} else {
				processIncludePath(clientEntity, include);
			}
		}

		processDefaultIncludes(clientEntity);
	}

	private void processIncludeArray(ResourceEntity<?> clientEntity, String include) {
		JsonNode root = jsonParser.parseJson(include);

		if (root != null && root.isArray()) {

			for (JsonNode child : root) {

				if (child.isObject()) {
					processIncludeObject(clientEntity, child);
				} else if (child.isTextual()) {
					processIncludePath(clientEntity, child.asText());
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
				expProcessor.process(includeEntity, expNode);
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

			ResourceEntity<T> mapByRoot = new ResourceEntity<T>(descriptor.getType(), descriptor.getCayenneEntity());
			processIncludePath(mapByRoot, mapByPath);
			descriptor.mapBy(mapByRoot, mapByPath);

		} else {
			LOGGER.info("Ignoring 'mapBy:" + mapByPath + "' for to-one relationship property");
		}
	}

	/**
	 * Records include path, returning null for the path corresponding to an
	 * attribute, and a child {@link ResourceEntity} for the path corresponding to
	 * relationship.
	 */
	// see TODO below
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
		ObjAttribute attribute = (ObjAttribute) parent.getCayenneEntity().getAttribute(property);
		if (attribute != null) {

			if (dot > 0) {
				throw new LinkRestException(Status.BAD_REQUEST, "Invalid include path: " + path);
			}

			parent.getAttributes().add(property);
			return null;
		}

		ObjRelationship relationship = (ObjRelationship) parent.getCayenneEntity().getRelationship(property);
		if (relationship != null) {

			ResourceEntity<?> childEntity = parent.getChild(property);
			if (childEntity == null) {
				// TODO: use ClassDescriptors to figure out the type of related
				// entity..
				childEntity = new ResourceEntity(metadataService.getType(relationship.getTargetEntityName()), relationship);
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

	private void processDefaultIncludes(ResourceEntity<?> clientEntity) {
		// either there are no includes (taking into account Id) or all includes
		// are relationships
		if (!clientEntity.isIdIncluded() && clientEntity.getAttributes().isEmpty()) {
			for (ObjAttribute oa : clientEntity.getCayenneEntity().getAttributes()) {
				clientEntity.getAttributes().add(oa.getName());
				clientEntity.getDefaultProperties().add(oa.getName());
			}
			// Id should be included by default
			clientEntity.includeId();
		}
	}

}
