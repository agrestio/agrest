package com.nhl.link.rest.parser.converter;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 1.10
 */
public abstract class AbstractConverter implements JsonValueConverter {

	@Override
	public Object value(JsonNode node) {
		if (node.isNull()) {
			return null;
		}

		return valueNonNull(node);
	}

	protected abstract Object valueNonNull(JsonNode node);
}
