package com.nhl.link.rest.parser.converter;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.JsonNode;

public class UtcDateConverter extends AbstractConverter {
	
	private static final JsonValueConverter instance = new UtcDateConverter();

	public static JsonValueConverter converter() {
		return instance;
	}

	@Override
	protected Object valueNonNull(JsonNode node) {
		return new DateTime(node.asText()).toDate();
	}
}
