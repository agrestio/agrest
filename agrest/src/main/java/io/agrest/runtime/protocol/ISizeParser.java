package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.protocol.Limit;
import io.agrest.protocol.Start;

/**
 * Parsing of Start and Limit query parameters from string or nested in Json values.
 *
 * @since 2.13
 */
public interface ISizeParser {

    Start startFromJson(JsonNode json);

    Limit limitFromJson(JsonNode json);
}
