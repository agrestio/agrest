package com.nhl.link.rest.parser.converter;

import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;

/**
 * @since 1.10
 */
public class LongConverter extends AbstractConverter {

	private static final JsonValueConverter instance = new LongConverter();

	public static JsonValueConverter converter() {
		return instance;
	}

	@Override
	protected Object valueNonNull(JsonNode node) {

		if (!node.isNumber()) {
			throw new LinkRestException(Status.BAD_REQUEST, "Expected 'long' value, got: " + node.asText());
		}

		return node.asLong();
	}

}
