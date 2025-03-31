package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;

/**
 * @since 5.0
 */
public class ByteConverter extends AbstractConverter<Byte> {

    private static final ByteConverter instance = new ByteConverter();

    public static ByteConverter converter() {
        return instance;
    }

    @Override
    protected Byte valueNonNull(JsonNode node) {

        if (!node.isNumber()) {
            throw AgException.badRequest("Expected numeric value, got: %s", node.asText());
        }

        int i = node.asInt();
        if (i < Byte.MIN_VALUE || i > Byte.MAX_VALUE) {
            throw AgException.badRequest("Value is out of range for 'java.lang.Byte': %s", node.asText());
        }

        return (byte) i;
    }
}
