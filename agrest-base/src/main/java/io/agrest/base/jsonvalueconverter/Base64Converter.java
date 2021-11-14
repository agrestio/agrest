package io.agrest.base.jsonvalueconverter;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.HttpStatus;

import java.util.Base64;

public class Base64Converter extends AbstractConverter<byte[]> {

	private static final Base64Converter instance = new Base64Converter();

	public static Base64Converter converter() {
		return instance;
	}

	@Override
	protected byte[] valueNonNull(JsonNode node) {

		if (!node.isTextual()) {
			throw new AgException(HttpStatus.BAD_REQUEST, "Expected textual value, got: " + node.asText());
		}
		try {
			return Base64.getDecoder().decode(node.asText());
		} catch (IllegalArgumentException e) {
			throw new AgException(HttpStatus.BAD_REQUEST, "Failed to decode Base64 value: " + node.asText(), e);
		}
	}
}
