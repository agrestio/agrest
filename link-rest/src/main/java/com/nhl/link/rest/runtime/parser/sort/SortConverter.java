package com.nhl.link.rest.runtime.parser.sort;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.runtime.parser.QueryParamConverter;
import com.nhl.link.rest.runtime.query.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.jackson.IJacksonService;

import java.util.ArrayList;
import java.util.List;

public class SortConverter extends QueryParamConverter<Sort> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SortConverter.class);

	private static final String PROPERTY = "property";
	private static final String DIRECTION = "direction";

	private IJacksonService jsonParser;

	SortConverter(IJacksonService jsonParser) {
		this.jsonParser = jsonParser;
	}

	@Override
	public Sort fromString(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}

		Sort sort;

		if (value.startsWith("[")) {
			List<Sort> sorts = fromArray(jsonParser.parseJson(value));
			sort = new Sort(sorts);
		} else if (value.startsWith("{")) {
			sort = value(jsonParser.parseJson(value));
		} else {
			sort = new Sort(value);
		}

		return sort;
	}

	@Override
	public Sort fromRootNode(JsonNode root) {
		JsonNode sortNode = root.get(Sort.getName());

		if (sortNode != null) {
			return fromString(sortNode.isTextual() ? sortNode.asText() : sortNode.toString());
		}

		return null;
	}

	@Override
	protected Sort valueNonNull(JsonNode node) {
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
			return new Sort(propertyNode.asText(), directionNode.asText());
		}

		return new Sort(propertyNode.asText());
	}

	private List<Sort> fromArray(JsonNode root) {
		List<Sort> sorts = new ArrayList<>();

		if (root != null && root.isArray()) {
			for (JsonNode sortNode : root) {
				Sort sort = value(sortNode);
				if (sort != null) {
					sorts.add(sort);
				}
			}
		}

		return sorts;
	}

}
