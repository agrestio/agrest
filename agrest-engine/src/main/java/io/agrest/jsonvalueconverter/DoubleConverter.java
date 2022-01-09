package io.agrest.jsonvalueconverter;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;

/**
 * @since 4.2
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
			throw AgException.badRequest("Expected numeric value, got: %s", node.asText());
		}

		return node.asDouble();
	}
}
