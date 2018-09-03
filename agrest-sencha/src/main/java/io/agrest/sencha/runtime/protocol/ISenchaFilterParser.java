package io.agrest.sencha.runtime.protocol;

import io.agrest.sencha.protocol.Filter;

import java.util.List;

/**
 * @since 2.13
 */
public interface ISenchaFilterParser {

    List<Filter> fromString(String filtersJson);
}
