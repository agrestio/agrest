package io.agrest.jsonvalueconverter;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;

import java.util.Base64;

public class Base64Converter extends AbstractConverter<byte[]> {

    private static final Base64Converter instance = new Base64Converter();

    public static Base64Converter converter() {
        return instance;
    }

    @Override
    protected byte[] valueNonNull(JsonNode node) {

        if (!node.isTextual()) {
            throw AgException.badRequest("Expected textual value, got: %s", node.asText());
        }

        try {
            return Base64.getDecoder().decode(node.asText());
        } catch (IllegalArgumentException e) {
            throw AgException.badRequest(e, "Failed to decode Base64 value: %s", node.asText());
        }
    }
}
