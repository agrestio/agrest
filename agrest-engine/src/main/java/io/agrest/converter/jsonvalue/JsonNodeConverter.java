package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 5.0
 */
public class JsonNodeConverter implements JsonValueConverter<JsonNode> {

    private static final JsonNodeConverter instance = new JsonNodeConverter();

    public static JsonNodeConverter converter() {
        return instance;
    }

    @Override
    public JsonNode value(JsonNode node) {
        return node;
    }
}
