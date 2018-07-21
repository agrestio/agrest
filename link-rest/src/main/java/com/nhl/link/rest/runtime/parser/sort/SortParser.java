package com.nhl.link.rest.runtime.parser.sort;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.protocol.Dir;
import com.nhl.link.rest.protocol.Sort;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

public class SortParser implements ISortParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(SortParser.class);

	private static final String PROPERTY = "property";
	private static final String DIRECTION = "direction";

	private IJacksonService jsonParser;

	public SortParser(@Inject IJacksonService jsonParser) {
		this.jsonParser = jsonParser;
	}

	@Override
	public Sort fromString(String sortValue) {
		if (sortValue == null || sortValue.isEmpty()) {
			return null;
		}

		if (sortValue.startsWith("[")) {
			List<Sort> sorts = fromArray(jsonParser.parseJson(sortValue));
			return new Sort(sorts);
		} else if (sortValue.startsWith("{")) {
			return fromJson(jsonParser.parseJson(sortValue));
		} else {
			return new Sort(sortValue);
		}
	}

	@Override
	public Sort fromRootNode(JsonNode root) {
		JsonNode sortNode = root.get(Sort.SORT);

		if (sortNode != null) {
			return fromString(sortNode.isTextual() ? sortNode.asText() : sortNode.toString());
		}

		return null;
	}

	@Override
	public Dir dirFromString(String dirValue) {
		if (dirValue != null) {
			if (dirValue.equals(Dir.ASC.name())) {
				return Dir.ASC;
			} else if (dirValue.equals(Dir.DESC.name())) {
				return Dir.DESC;
			} else {
				throw new LinkRestException(Response.Status.BAD_REQUEST, "Direction is invalid: " + dirValue);
			}
		}
		return null;
	}

	private Sort fromJson(JsonNode node) {
		if (node.isNull()) {
			return null;
		}
		JsonNode propertyNode = node.get(PROPERTY);
		if (propertyNode == null || !propertyNode.isTextual()) {

			// this is a hack for Sencha bug, passing us null sorters
			// per LF-189...
			// So allowing for lax property name checking as a result
			if (propertyNode != null && propertyNode.isNull()) {
				LOGGER.info("ignoring NULL sort property");
				return null;
			}

			throw new LinkRestException(Status.BAD_REQUEST, "Bad sort spec: " + node);
		}

		JsonNode directionNode = node.get(DIRECTION);
		if (directionNode != null) {
			Dir dir = dirFromString(directionNode.asText());
			if (dir != null) {
				return new Sort(propertyNode.asText(), dir);
			}
		}

		return new Sort(propertyNode.asText());
	}

	private List<Sort> fromArray(JsonNode root) {
		List<Sort> sorts = new ArrayList<>();

		if (root != null && root.isArray()) {
			for (JsonNode sortNode : root) {
				Sort sort = fromJson(sortNode);
				if (sort != null) {
					sorts.add(sort);
				}
			}
		}

		return sorts;
	}

}
