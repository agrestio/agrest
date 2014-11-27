package com.nhl.link.rest.parser.converter;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 1.10
 */
public class GenericConverter implements JsonValueConverter {
	
	private static final JsonValueConverter instance = new GenericConverter();

	public static JsonValueConverter converter() {
		return instance;
	}

	@Override
	public Object value(JsonNode valueNode) {
		JsonToken type = valueNode.asToken();

		switch (type) {
		case VALUE_NUMBER_INT:
			return valueNode.asInt();
		case VALUE_NUMBER_FLOAT:
			return valueNode.asDouble();
		case VALUE_TRUE:
			return Boolean.TRUE;
		case VALUE_FALSE:
			return Boolean.FALSE;
		case VALUE_NULL:
			return null;
		default:
			return valueNode.asText();
		}
	}

}
