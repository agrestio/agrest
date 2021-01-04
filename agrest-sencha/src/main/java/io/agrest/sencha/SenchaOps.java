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

    public static <T> Consumer<SelectContext<T>> startsWithFilter(String property, UriInfo uriInfo) {
        String value = ParameterExtractor.string(uriInfo.getQueryParameters(), AgProtocolSenchaExt.query);
        return startsWithFilter(property, value);
    }

    public static <T> Consumer<SelectContext<T>> startsWithFilter(String property, String value) {
        return c -> StartsWithFilter
                .getInstance()
                .filter(c, property, value)
                .ifPresent(exp -> c.getEntity().andQualifier(exp));
    }
}
