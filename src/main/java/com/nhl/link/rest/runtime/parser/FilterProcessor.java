package com.nhl.link.rest.runtime.parser;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.map.ObjEntity;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhl.link.rest.ClientEntity;
import com.nhl.link.rest.LinkRestException;

class FilterProcessor {

	private static final int MAX_VALUE_LENGTH = 1024;
	private static final String PROPERTY = "property";
	private static final String VALUE = "value";

	private RequestJsonParser jsonParser;
	private PathCache pathCache;

	FilterProcessor(RequestJsonParser jsonParser, PathCache pathCache) {
		this.jsonParser = jsonParser;
		this.pathCache = pathCache;
	}

	void process(ClientEntity<?> clientEntity, String filtersJson) {
		if (filtersJson == null || filtersJson.length() == 0) {
			return;
		}

		JsonNode rootNode = jsonParser.parseJSON(filtersJson, new ObjectMapper());
		if (rootNode == null) {
			return;
		}

		for (JsonNode filterNode : rootNode) {
			JsonNode propertyNode = filterNode.get(PROPERTY);
			if (propertyNode == null) {
				throw new LinkRestException(Status.BAD_REQUEST, "filter 'property' is missing" + filterNode.asText());
			}

			JsonNode valueNode = filterNode.get(VALUE);
			if (valueNode == null) {
				throw new LinkRestException(Status.BAD_REQUEST, "filter 'value' is missing" + filterNode.asText());
			}

			String property = propertyNode.asText();

			Object valueUnescaped = extractValue(valueNode);

			Expression qualifier;
			if (valueUnescaped == null) {
				qualifier = ExpressionFactory.matchExp(property, null);
			} else if (valueUnescaped instanceof Boolean) {
				qualifier = ExpressionFactory.matchExp(property, valueUnescaped);
			} else {
				checkValueLength((String) valueUnescaped);
				String value = escapeValueForLike((String) valueUnescaped) + "%";
				qualifier = ExpressionFactory.likeIgnoreCaseExp(property, value);
			}

			// validate property path
			ObjEntity rootEntity = clientEntity.getEntity();
			PathDescriptor pd = pathCache.entityPathCache(rootEntity).getPathDescriptor(
					(ASTObjPath) qualifier.getOperand(0));

			if (!pd.isAttribute()) {
				throw new LinkRestException(Status.BAD_REQUEST, "filter 'property' points to a relationship'"
						+ property + "'. Can't filter on relationships");
			}

			clientEntity.andQualifier(qualifier);
		}
	}

	private static Object extractValue(JsonNode valueNode) {
		JsonToken type = valueNode.asToken();

		// ExtJS converts everything to String except for NULL and booleans. So
		// follow the
		// same logic here...
		// (http://docs.sencha.com/extjs/4.1.2/source/Filter.html#Ext-util-Filter)
		switch (type) {
		case VALUE_NULL:
			return null;
		case VALUE_FALSE:
			return false;
		case VALUE_TRUE:
			return true;
		default:
			return valueNode.asText();
		}
	}

	static String escapeValueForLike(String value) {
		int len = value.length();

		StringBuilder out = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c = value.charAt(i);
			if (c == '_' || c == '%') {
				out.append('\\');
			}

			out.append(c);
		}

		return out.toString();
	}

	private void checkValueLength(String value) {
		if (value.length() > MAX_VALUE_LENGTH) {
			throw new LinkRestException(Status.BAD_REQUEST, "filter 'value' is to long: " + value);
		}
	}
}
