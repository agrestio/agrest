package com.nhl.link.rest.parser.converter;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Converts leaf JSON nodes to Java objects.
 * 
 * @since 1.10
 */
public interface JsonValueConverter {

	Object value(JsonNode node);
}
