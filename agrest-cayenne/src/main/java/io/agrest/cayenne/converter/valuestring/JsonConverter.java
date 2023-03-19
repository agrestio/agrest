package io.agrest.cayenne.converter.valuestring;

import io.agrest.converter.valuestring.AbstractConverter;
import org.apache.cayenne.value.Json;

/**
 * @since 5.0
 */
public class JsonConverter extends AbstractConverter<Json> {

    private static final JsonConverter instance = new JsonConverter();

    public static JsonConverter converter() {
        return instance;
    }

    private JsonConverter() {
    }

    @Override
    protected String asStringNonNull(Json json) {
        return json.getRawJson();
    }
}
