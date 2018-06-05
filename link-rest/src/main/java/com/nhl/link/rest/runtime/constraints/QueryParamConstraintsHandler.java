package com.nhl.link.rest.runtime.constraints;

import com.nhl.link.rest.constraints.ConstrainedLrEntity;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @since 2.13
 */
public class QueryParamConstraintsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryParamConstraintsHandler.class);


    <T> boolean constrainRead(SelectContext<T> context, Constraint<T> constraint) {

        if (context == null) {
            return true;
        }

        if (constraint == null) {
            return true;
        }

        return applyForRead(context, constraint.apply(context.getEntity().getLrEntity()));
    }

    private boolean applyForRead(SelectContext context, ConstrainedLrEntity constraint) {
        boolean result = true;
        final MultivaluedMap<String, String> queryParams = context.getUriInfo().getQueryParameters();

        for (String param : queryParams.keySet()) {
            if (!((ConstrainedLrEntity)constraint).hasQueryParam(param)) {
                LOGGER.warn(String.format("The query parameter '%s' is not supported", param));
                result = false;
            }
        }
        return result;
    }
}
