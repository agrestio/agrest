package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Converts leaf JSON nodes to Java objects.
 * 
 * @since 1.10
 */
public interface JsonValueConverter<T> {

	T value(JsonNode node);
}
