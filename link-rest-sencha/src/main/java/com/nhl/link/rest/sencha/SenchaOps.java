package com.nhl.link.rest.sencha;

import com.nhl.link.rest.runtime.protocol.ParameterExtractor;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.sencha.ops.StartsWithFilter;
import org.apache.cayenne.exp.Expression;
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
        String value = ParameterExtractor.string(uriInfo.getQueryParameters(), QUERY);
        return startsWithFilter(property, value);
    }

    public static <T> Consumer<SelectContext<T>> startsWithFilter(Property<?> property, String value) {
        return c -> StartsWithFilter
                .getInstance()
                .filter(c, property.getName(), value)
                .ifPresent(exp -> processStartsWith(c, exp));
    }

    private static void processStartsWith(SelectContext<?> context, Expression expression) {

        // we can be called from different stages of the pipeline. Context may or may not have all the pieces in place..

        if (context.getEntity() != null) {
            context.getEntity().andQualifier(expression);
        }
        // else - error?

        if (context.getSelect() != null) {
            context.getSelect().andQualifier(expression);
        }
    }
}
