package com.nhl.link.rest.parser.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;

public class Base64Converter extends AbstractConverter {

    private static final JsonValueConverter instance = new Base64Converter();

	public static JsonValueConverter converter() {
		return instance;
	}

    @Override
    protected Object valueNonNull(JsonNode node) {

        if (!node.isTextual()) {
            throw new LinkRestException(Status.BAD_REQUEST, "Expected textual value, got: " + node.asText());
        }
        try {
            return new sun.misc.BASE64Decoder().decodeBuffer(node.asText());
        } catch (IOException e) {
            throw new LinkRestException(Status.BAD_REQUEST, "Failed to decode Base64 value: " + node.asText());
        }
    }
}
