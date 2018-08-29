package com.nhl.link.rest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.protocol.Limit;
import com.nhl.link.rest.protocol.Start;

/**
 * Parsing of Start and Limit query parameters from string or nested in Json values.
 *
 * @since 2.13
 */
public interface ISizeParser {

    Start startFromJson(JsonNode json);

    Limit limitFromJson(JsonNode json);
}
