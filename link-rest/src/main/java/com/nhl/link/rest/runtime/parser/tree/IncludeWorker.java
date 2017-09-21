package com.nhl.link.rest.runtime.parser.tree;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.function.FunctionProcessor;
import com.nhl.link.rest.runtime.parser.tree.function.FunctionalIncludeVisitor;
import org.apache.cayenne.exp.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.Map;

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
	private PathProcessor pathProcessor;
	private FunctionalIncludeVisitor functionalIncludeVisitor;

	public IncludeWorker(IJacksonService jsonParser,
						 ISortProcessor sortProcessor,
						 ICayenneExpProcessor expProcessor,
						 Map<String, FunctionProcessor> functionProcessors) {
		this.jsonParser = jsonParser;
		this.sortProcessor = sortProcessor;
		this.expProcessor = expProcessor;
		this.pathProcessor = PathProcessor.processor();
		this.functionalIncludeVisitor = new FunctionalIncludeVisitor(functionProcessors);
	}

	public void process(ResourceEntity<?> resourceEntity, List<String> includes) {
		for (String include : includes) {

			if (include.startsWith("[")) {
				processIncludeArray(resourceEntity, include);
			} else if (include.startsWith("{")) {
				JsonNode root = jsonParser.parseJson(include);
				processIncludeObject(resourceEntity, root);
			} else {
				pathProcessor.processPath(resourceEntity, include, functionalIncludeVisitor);
			}
		}

		applyDefaultIncludes(resourceEntity);
	}

	private void processIncludeArray(ResourceEntity<?> resourceEntity, String include) {
		JsonNode root = jsonParser.parseJson(include);

		if (root != null && root.isArray()) {

			for (JsonNode child : root) {

				if (child.isObject()) {
					processIncludeObject(resourceEntity, child);
				} else if (child.isTextual()) {
					pathProcessor.processPath(resourceEntity, child.asText(), functionalIncludeVisitor);
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
				includeEntity = pathProcessor.processPath(rootEntity, path, functionalIncludeVisitor);
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
			// using standard include visitor, because functions don't make sense in the context of mapBy
			pathProcessor.processPath(mapByRoot, mapByPath, IncludeVisitor.visitor());
			descriptor.mapBy(mapByRoot, mapByPath);

		} else {
			LOGGER.info("Ignoring 'mapBy:" + mapByPath + "' for to-one relationship property");
		}
	}

	static void applyDefaultIncludes(ResourceEntity<?> resourceEntity) {
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
