package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;

import java.math.BigDecimal;

/**
 * @since 4.7
 */
public class BigDecimalConverter extends AbstractConverter<BigDecimal> {

    private static final BigDecimalConverter instance = new BigDecimalConverter();

    public static BigDecimalConverter converter() {
        return instance;
    }

    @Override
    protected BigDecimal valueNonNull(JsonNode node) {

        try {
            // don't attempt to call "node.asDouble()". Creating a BigDecimal from a double instead of a String
            // will result in fake precision explosion
            return new BigDecimal(node.asText());
        } catch (NumberFormatException e) {
            throw AgException.badRequest("Expected numeric value, got: %s", node.asText());
        }
    }
}
