package com.nhl.link.rest.runtime.parser.sort;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.protocol.Dir;
import com.nhl.link.rest.protocol.Sort;

/**
 * Parsing of Sort and Dir query parameters from string or nested in Json values.
 *
 * @since 2.13
 */
public interface ISortParser {

    Sort fromString(String sortValue, String dirValue);

    Dir dirFromString(String dirValue);

    Sort fromRootNode(JsonNode root);
}
