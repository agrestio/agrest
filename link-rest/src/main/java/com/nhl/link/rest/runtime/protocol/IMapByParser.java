package com.nhl.link.rest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.protocol.MapBy;

/**
 * Parsing of MapBy query parameter from string or nested in Json values.
 *
 * @since 2.13
 */
public interface IMapByParser {

    MapBy fromString(String value);

    MapBy fromJson(JsonNode json);
}
