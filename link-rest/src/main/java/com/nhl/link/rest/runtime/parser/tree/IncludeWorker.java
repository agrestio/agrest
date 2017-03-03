package com.nhl.link.rest.runtime.parser.tree;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
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
				resourceEntity.includePath(include);
			}
		}

		resourceEntity.includeDefaults();
	}

	private void processIncludeArray(ResourceEntity<?> resourceEntity, String include) {
		JsonNode root = jsonParser.parseJson(include);

		if (root != null && root.isArray()) {

			for (JsonNode child : root) {

				if (child.isObject()) {
					processIncludeObject(resourceEntity, child);
				} else if (child.isTextual()) {
					resourceEntity.includePath(child.asText());
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
				includeEntity = rootEntity.includePath(path);
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

			ResourceEntity<T> mapByRoot = new ResourceEntity<>(descriptor.getLrEntity());
			mapByRoot.includePath(mapByPath);
			descriptor.mapBy(mapByRoot, mapByPath);

		} else {
			LOGGER.info("Ignoring 'mapBy:" + mapByPath + "' for to-one relationship property");
		}
	}
}
