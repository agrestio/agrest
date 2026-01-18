package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 1.10
 */
public class GenericConverter implements JsonValueConverter<Object> {
	
	private static final JsonValueConverter<Object> instance = new GenericConverter();

	public static JsonValueConverter<Object> converter() {
		return instance;
	}

	@Override
	public Object value(JsonNode valueNode) {
		JsonToken type = valueNode.asToken();

        return switch (type) {
            case VALUE_NUMBER_INT -> valueNode.asInt();
            case VALUE_NUMBER_FLOAT -> valueNode.asDouble();
            case VALUE_TRUE -> Boolean.TRUE;
            case VALUE_FALSE -> Boolean.FALSE;
            case VALUE_NULL -> null;
            default -> valueNode.asText();
        };
	}

}
