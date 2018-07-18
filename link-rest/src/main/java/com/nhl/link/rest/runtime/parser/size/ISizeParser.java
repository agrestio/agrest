package com.nhl.link.rest.runtime.parser.size;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.runtime.query.Limit;
import com.nhl.link.rest.runtime.query.Start;

/**
 * Parsing of Start and Limit query parameters from string or nested in Json values.
 *
 * @since 2.13
 */
public interface ISizeParser {

    Start startFromRootNode(JsonNode root);

    Limit limitFromRootNode(JsonNode root);
}
