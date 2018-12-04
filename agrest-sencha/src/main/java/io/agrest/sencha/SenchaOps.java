package io.agrest.sencha;

import io.agrest.runtime.cayenne.converter.CayenneExpressionConverter;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.protocol.ParameterExtractor;
import io.agrest.sencha.ops.StartsWithFilter;
import io.agrest.backend.exp.Expression;

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
                .ifPresent(exp -> processStartsWith(c, exp));
    }

    private static void processStartsWith(SelectContext<?> context, Expression expression) {

        // we can be called from different stages of the pipeline. Context may or may not have all the pieces in place..

        if (context.getEntity() != null) {
            context.getEntity().andQualifier(expression);
        }
        // else - error?

        if (context.getSelect() != null) {
            context.getSelect().andQualifier(new CayenneExpressionConverter().apply(expression));
        }
    }
}
