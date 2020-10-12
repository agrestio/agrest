package io.agrest.sencha;

import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.protocol.ParameterExtractor;
import io.agrest.sencha.ops.StartsWithFilter;

import javax.ws.rs.core.UriInfo;
import java.util.function.Consumer;

/**
 * Provides common Sencha filters.
 *
 * @since 2.13
 */
public class SenchaOps {

    private static final String QUERY = "query";

    public static <T> Consumer<SelectContext<T>> startsWithFilter(String property, UriInfo uriInfo) {
        String value = ParameterExtractor.string(uriInfo.getQueryParameters(), QUERY);
        return startsWithFilter(property, value);
    }

    public static <T> Consumer<SelectContext<T>> startsWithFilter(String property, String value) {
        return c -> StartsWithFilter
                .getInstance()
                .filter(c, property, value)
                .ifPresent(exp -> c.getEntity().getQualifiers().add(exp));
    }
}
