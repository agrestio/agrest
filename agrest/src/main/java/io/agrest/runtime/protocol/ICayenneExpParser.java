package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.protocol.CayenneExp;

/**
 * Parsing of CayenneExp query parameter from string or nested in Json values.
 *
 * @since 2.13
 */
public interface ICayenneExpParser {

    CayenneExp fromString(String value);

    CayenneExp fromJson(JsonNode json);
}
