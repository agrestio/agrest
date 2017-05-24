package com.nhl.link.rest.parser.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;

import javax.ws.rs.core.Response.Status;

/**
 * @since 1.10
 */
public class FloatConverter extends AbstractConverter {

	private static final JsonValueConverter instance = new FloatConverter();

	public static JsonValueConverter converter() {
		return instance;
	}

	@Override
	protected Object valueNonNull(JsonNode node) {
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

		if (!node.isFloatingPointNumber()) {
			throw new LinkRestException(Status.BAD_REQUEST, "Expected floating point numeric value, got: " + node.asText());
		}

		Double doubleValue = node.asDouble();
		if (Double.valueOf(0).equals(doubleValue)) {
			return 0f;
		}

		Double absDoubleValue = Math.abs(doubleValue);
		if (absDoubleValue < Float.MIN_VALUE || absDoubleValue > Float.MAX_VALUE) {
			throw new LinkRestException(Status.BAD_REQUEST,
					"Value is either too small or too large for the java.lang.Float datatype: " + node.asText());
		}
		return doubleValue.floatValue();
	}

}
