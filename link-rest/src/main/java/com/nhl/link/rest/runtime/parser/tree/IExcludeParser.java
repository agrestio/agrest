package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.protocol.Exclude;

import java.util.List;

/**
 * Parsing of Exclude query parameter from string value.
 *
 * @since 2.13
 */
public interface IExcludeParser {

    List<Exclude> fromStrings(List<String> values);
}
