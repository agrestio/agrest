package io.agrest.runtime.protocol;

import io.agrest.protocol.Exclude;

/**
 * Parsing of Exclude query parameter from string value.
 *
 * @since 2.13
 */
public interface IExcludeParser {

    Exclude parse(String value);
}
