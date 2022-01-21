package io.agrest.cayenne.converter.valuestring;

import io.agrest.converter.valuestring.AbstractConverter;
import io.agrest.converter.valuestring.ValueStringConverter;
import org.apache.cayenne.value.Json;

/**
 * @since 5.0
 */
public class JsonConverter extends AbstractConverter {

    private static final ValueStringConverter instance = new JsonConverter();

    public static ValueStringConverter converter() {
        return instance;
    }

    private JsonConverter() {
    }

    @Override
    protected String asStringNonNull(Object object) {
        Json json = (Json) object;
        return json.getRawJson();
    }
}
