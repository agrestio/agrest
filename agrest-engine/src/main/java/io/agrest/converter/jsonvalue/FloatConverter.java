package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;

/**
 * @since 1.10
 */
public class FloatConverter extends AbstractConverter<Float> {

    private static final FloatConverter instance = new FloatConverter();

    public static FloatConverter converter() {
        return instance;
    }

    @Override
    protected Float valueNonNull(JsonNode node) {
        if (node.isTextual()) {
            String value = node.asText();
            if ("NaN".equalsIgnoreCase(value)) {
                return Float.NaN;
            } else if ("infinity".equalsIgnoreCase(value) || "+infinity".equalsIgnoreCase(value)) {
                return Float.POSITIVE_INFINITY;
            } else if ("-infinity".equalsIgnoreCase(value)) {
                return Float.NEGATIVE_INFINITY;
            }
        }

        if (!node.isIntegralNumber() && !node.isFloatingPointNumber()) {
            throw AgException.badRequest("Expected numeric value, got: %s", node.asText());
        }

        Double doubleValue = node.asDouble();
        if (Double.valueOf(0).equals(doubleValue)) {
            return 0f;
        }

        Double absDoubleValue = Math.abs(doubleValue);
        if (absDoubleValue < Float.MIN_VALUE || absDoubleValue > Float.MAX_VALUE) {
            throw AgException.badRequest("Value is out of range for 'java.lang.Float': %s", node.asText());
        }

        return doubleValue.floatValue();
    }

}
