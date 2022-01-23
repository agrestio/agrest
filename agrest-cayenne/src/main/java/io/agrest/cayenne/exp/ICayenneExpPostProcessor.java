package io.agrest.cayenne.exp;

import io.agrest.AgException;
import io.agrest.meta.AgEntity;
import org.apache.cayenne.exp.Expression;

/**
 * @since 2.2
 */
public interface ICayenneExpPostProcessor {

    /**
     * Performs any post-processing and cleanup that might be necessary for the successful execution of expression
     *
     * @return Expression that is ready for execution
     * @throws AgException if expression is malformed or violates validation constraints
     */
    Expression process(AgEntity<?> entity, Expression exp);
}
