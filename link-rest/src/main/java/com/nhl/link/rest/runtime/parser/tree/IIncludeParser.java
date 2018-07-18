package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.runtime.query.Include;

import java.util.List;

/**
 * Parsing of Include query parameter from string value.
 *
 * @since 2.13
 */
public interface IIncludeParser {

    List<Include> fromStrings(List<String> values);
}
