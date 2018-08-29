package com.nhl.link.rest.sencha.runtime.protocol;

import com.nhl.link.rest.sencha.protocol.Filter;

import java.util.List;

/**
 * @since 2.13
 */
public interface ISenchaFilterParser {

    List<Filter> fromString(String filtersJson);
}
