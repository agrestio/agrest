package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Parsing of Start and Limit query parameters from string or related in JSON values.
 *
 * @since 2.13
 */
public interface ISizeParser {

    Integer startFromJson(JsonNode json);

    Integer limitFromJson(JsonNode json);
}
