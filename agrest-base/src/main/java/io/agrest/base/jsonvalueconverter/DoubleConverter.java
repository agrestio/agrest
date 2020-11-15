package io.agrest.base.jsonvalueconverter;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;

import javax.ws.rs.core.Response.Status;

/**
 * @since 3.8
 */
public class DoubleConverter extends AbstractConverter<Double> {

	private static final DoubleConverter instance = new DoubleConverter();

	public static DoubleConverter converter() {
		return instance;
	}

	@Override
	protected Double valueNonNull(JsonNode node) {
		if (node.isTextual()) {
			String value = node.asText();
			if ("NaN".equalsIgnoreCase(value)) {
				return Double.NaN;
			} else if ("infinity".equalsIgnoreCase(value) || "+infinity".equalsIgnoreCase(value)) {
				return Double.POSITIVE_INFINITY;
			} else if ("-infinity".equalsIgnoreCase(value)) {
				return Double.NEGATIVE_INFINITY;
			}
		}

		if (!node.isIntegralNumber() && !node.isFloatingPointNumber()) {
			throw new AgException(Status.BAD_REQUEST, "Expected numeric value, got: " + node.asText());
		}

		return node.asDouble();
	}
}
