package io.agrest.cayenne.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.converter.jsonvalue.AbstractConverter;
import org.apache.cayenne.value.Json;

/**
 * @since 4.1
 */
public class JsonConverter extends AbstractConverter<Json> {

    private static final JsonConverter instance = new JsonConverter();

    public static JsonConverter converter() {
        return instance;
    }

    @Override
    protected Json valueNonNull(JsonNode node) {
        return new Json(node.toString());
    }
}
