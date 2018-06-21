package com.nhl.link.rest.sencha;

import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.sencha.ops.StartsWithFilter;
import org.apache.cayenne.exp.Property;

import javax.ws.rs.core.UriInfo;
import java.util.function.Consumer;

/**
 * Provides common Sencha filters.
 *
 * @since 2.13
 */
public class SenchaOps {

    private static final String QUERY = "query";

    public static <T> Consumer<SelectContext<T>> startsWithFilter(Property<?> property, UriInfo uriInfo) {
        String value = BaseRequestProcessor.string(uriInfo.getQueryParameters(), QUERY);
        return startsWithFilter(property, value);
    }

    public static <T> Consumer<SelectContext<T>> startsWithFilter(Property<?> property, String value) {
        return c -> StartsWithFilter
                .getInstance()
                .filter(c, property.getName(), value)
                .ifPresent(c.getSelect()::andQualifier);
    }
}
