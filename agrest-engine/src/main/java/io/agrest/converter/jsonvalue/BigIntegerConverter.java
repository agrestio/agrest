package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;

import java.math.BigInteger;

/**
 * @since 5.0
 */
public class BigIntegerConverter extends AbstractConverter<BigInteger> {

    private static final BigIntegerConverter instance = new BigIntegerConverter();

    public static BigIntegerConverter converter() {
        return instance;
    }

    @Override
    protected BigInteger valueNonNull(JsonNode node) {

        try {
            return new BigInteger(node.asText());
        } catch (NumberFormatException e) {
            throw AgException.badRequest("Expected numeric value, got: %s", node.asText());
        }
    }
}
