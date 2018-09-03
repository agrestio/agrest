package io.agrest.parser.converter;

import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.LinkRestException;

/**
 * @since 1.10
 */
public class LongConverter extends AbstractConverter<Long> {

	private static final LongConverter instance = new LongConverter();

	public static LongConverter converter() {
		return instance;
	}

	@Override
	protected Long valueNonNull(JsonNode node) {

		if (!node.isNumber()) {
			throw new LinkRestException(Status.BAD_REQUEST, "Expected 'long' value, got: " + node.asText());
		}

		return node.asLong();
	}

}
