package com.nhl.link.rest.runtime.parser.sort;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ParamConverter;

import com.nhl.link.rest.runtime.query.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.jackson.IJacksonService;

import java.util.ArrayList;
import java.util.List;

class SortConverter implements ParamConverter<Sort> {

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

		Sort sort = new Sort();

		if (value.startsWith("[")) {
			List<Sort> sorts = fromArray(jsonParser.parseJson(value));
			sort.setSorts(sorts);
		} else if (value.startsWith("{")) {
			sort = getSortObject(jsonParser.parseJson(value));
		} else {
			sort.setProperty(value);
		}

		return sort;
	}

	@Override
	public String toString(Sort value) {
		return null;
	}

	List<Sort> fromArray(JsonNode root) {
		List<Sort> sorts = new ArrayList<>();

		if (root != null && root.isArray()) {
			for (JsonNode sortNode : root) {
				Sort sort = getSortObject(sortNode);
				if (sort != null) {
					sorts.add(sort);
				}
			}
		}

		return sorts;
	}

	Sort getSortObject(JsonNode sortNode) {
		JsonNode propertyNode = sortNode.get(PROPERTY);
		if (propertyNode == null || !propertyNode.isTextual()) {

			// this is a hack for Sencha bug, passing us null sorters
			// per LF-189...
			// So allowing for lax property name checking as a result
			if (propertyNode != null && propertyNode.isNull()) {
				LOGGER.info("ignoring NULL sort property");
				return null;
			}

			throw new LinkRestException(Status.BAD_REQUEST, "Bad sort spec: " + sortNode);
		}

		Sort sort = new Sort();
		sort.setProperty(propertyNode.asText());

		JsonNode directionNode = sortNode.get(DIRECTION);
		if (directionNode != null) {
			sort.setDirection(directionNode.asText());
		}

		return sort;
	}

}
