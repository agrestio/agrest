package io.agrest.runtime.protocol;

import io.agrest.protocol.Include;

import java.util.List;

/**
 * Parsing of Include query parameter from string value.
 *
 * @since 2.13
 */
public interface IIncludeParser {

    List<Include> fromStrings(List<String> values);

    Include oneFromString(String value);
}
