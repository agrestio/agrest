package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 5.0
 */
public class MapConverter extends AbstractConverter<Map<?, ?>> {

    private final Map<String, JsonValueConverter<?>> propertyConverters;
    private final int capacity;

    public MapConverter(Map<String, JsonValueConverter<?>> propertyConverters) {
        this.propertyConverters = propertyConverters;
        this.capacity = (int) Math.ceil(propertyConverters.size() * 4 / 3.);
    }

    @Override
    protected Map<?, ?> valueNonNull(JsonNode node) {

        if (!node.isObject()) {
            throw AgException.badRequest("Expected object value, got: %s", node.asText());
        }

        Map<String, Object> map = new HashMap<>(capacity);
        propertyConverters.forEach((k, v) -> map.put(k, v.value(node.get(k))));
        return map;
    }
}
