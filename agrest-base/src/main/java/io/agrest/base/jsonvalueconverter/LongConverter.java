package io.agrest.base.jsonvalueconverter;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.HttpStatus;

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
			throw new AgException(HttpStatus.BAD_REQUEST, "Expected 'long' value, got: " + node.asText());
		}

		return node.asLong();
	}

}
