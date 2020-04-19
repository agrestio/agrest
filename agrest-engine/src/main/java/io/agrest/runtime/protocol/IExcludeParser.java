package io.agrest.runtime.protocol;

import io.agrest.base.protocol.Exclude;

import java.util.List;

/**
 * Parsing of Exclude query parameter from string value.
 *
 * @since 2.13
 */
public interface IExcludeParser {

    List<Exclude> parse(String unparsed);
}
