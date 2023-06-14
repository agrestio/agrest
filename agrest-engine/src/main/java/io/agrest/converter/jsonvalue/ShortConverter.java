package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;

/**
 * @since 1.10
 */
public class ShortConverter extends AbstractConverter<Short> {

    private static final ShortConverter instance = new ShortConverter();

    public static ShortConverter converter() {
        return instance;
    }

    @Override
    protected Short valueNonNull(JsonNode node) {

        if (!node.isNumber()) {
            throw AgException.badRequest("Expected numeric value, got: %s", node.asText());
        }

        int i = node.asInt();
        if (i < Short.MIN_VALUE || i > Short.MAX_VALUE) {
            throw AgException.badRequest("Value is out of range for 'java.lang.Short': %s", node.asText());
        }

        return (short) i;
    }
}
