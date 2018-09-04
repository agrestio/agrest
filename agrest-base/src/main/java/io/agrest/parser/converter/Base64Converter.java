package io.agrest.parser.converter;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgRESTException;

public class Base64Converter extends AbstractConverter<byte[]> {

	private static final Base64Converter instance = new Base64Converter();

	public static Base64Converter converter() {
		return instance;
	}

	@Override
	protected byte[] valueNonNull(JsonNode node) {

		if (!node.isTextual()) {
			throw new AgRESTException(Status.BAD_REQUEST, "Expected textual value, got: " + node.asText());
		}
		try {
			// TODO: replace with Base64.Decoder when we upgrade to java 8
			return DatatypeConverter.parseBase64Binary(node.asText());
		} catch (IllegalArgumentException e) {
			throw new AgRESTException(Status.BAD_REQUEST, "Failed to decode Base64 value: " + node.asText(), e);
		}
	}
}
