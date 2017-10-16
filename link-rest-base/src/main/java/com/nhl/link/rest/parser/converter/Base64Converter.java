package com.nhl.link.rest.parser.converter;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;

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
			// TODO: replace with Base64.Decoder when we upgrade to java 8
			return DatatypeConverter.parseBase64Binary(node.asText());
		} catch (IllegalArgumentException e) {
			throw new LinkRestException(Status.BAD_REQUEST, "Failed to decode Base64 value: " + node.asText(), e);
		}
	}
}
