package io.agrest.base.jsonvalueconverter;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 1.10
 */
public abstract class AbstractConverter<T> implements JsonValueConverter<T> {

	@Override
	public T value(JsonNode node) {
		if (node.isNull()) {
			return null;
		}

		return valueNonNull(node);
	}

	protected abstract T valueNonNull(JsonNode node);
}
