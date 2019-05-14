package io.agrest.runtime.protocol;

import io.agrest.protocol.Include;

/**
 * Parsing of Include query parameter from string value.
 *
 * @since 2.13
 */
public interface IIncludeParser {

    Include parse(String value);
}
