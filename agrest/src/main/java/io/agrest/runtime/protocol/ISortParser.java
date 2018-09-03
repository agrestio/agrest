package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.protocol.Dir;
import io.agrest.protocol.Sort;

/**
 * Parsing of Sort and Dir query parameters from string or nested in Json values.
 *
 * @since 2.13
 */
public interface ISortParser {

    Sort fromString(String path);

    Sort fromJson(JsonNode json);

    Dir dirFromString(String dirValue);

}
