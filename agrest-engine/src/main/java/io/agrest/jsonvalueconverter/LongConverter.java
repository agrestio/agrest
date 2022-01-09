package io.agrest.jsonvalueconverter;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;

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
			throw AgException.badRequest("Expected numeric value, got: %s", node.asText());
		}

		return node.asLong();
	}

}
