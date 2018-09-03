package io.agrest.sencha.ops;

import io.agrest.LinkRestException;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;

import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * @since 2.13
 */
public class StartsWithFilter {

    private static final StartsWithFilter INSTANCE = new StartsWithFilter();

    public static StartsWithFilter getInstance() {
        return INSTANCE;
    }

    public Optional<Expression> filter(SelectContext<?> context, String queryProperty, String value) {

        if (value == null || value.length() == 0 || queryProperty == null) {
            return Optional.empty();
        }

        AgEntity<?> entity = context.getEntity().getAgEntity();

        validateAttribute(entity, queryProperty);

        value = FilterUtil.escapeValueForLike(value) + "%";
        return Optional.of(ExpressionFactory.likeIgnoreCaseExp(queryProperty, value));
    }

    /**
     * Checks that the user picked a valid property to compare against. Since
     * any bad args were selected by the server-side code, return 500 response.
     */
    private void validateAttribute(AgEntity<?> entity, String queryProperty) {
        AgAttribute attribute = entity.getAttribute(queryProperty);
        if (attribute == null) {
            throw new LinkRestException(Response.Status.INTERNAL_SERVER_ERROR, "No such property '" + queryProperty
                    + "' for entity '" + entity.getName() + "'");
        } else if (!String.class.equals(attribute.getType())) {
            throw new LinkRestException(Response.Status.INTERNAL_SERVER_ERROR,
                    "Invalid property type for query comparison: '" + queryProperty + "' for entity '"
                            + entity.getName() + "'");
        }
    }
}
