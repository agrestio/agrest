package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.protocol.Exp;

/**
 * Parsing of CayenneExp query parameter from string or nested in Json values.
 *
 * @since 2.13
 */
public interface IExpParser {

    Exp fromString(String value);

    Exp fromJson(JsonNode json);
}
