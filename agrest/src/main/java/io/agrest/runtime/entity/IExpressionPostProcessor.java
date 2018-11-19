package io.agrest.runtime.entity;

import io.agrest.AgException;
import io.agrest.meta.AgEntity;
import io.agrest.backend.exp.Expression;

/**
 * @since 2.2
 */
public interface IExpressionPostProcessor {

    /**
     * Perform any post-processing and cleanup that might be necessary for the successful execution of expression
     * @return Expression that is ready for execution
     * @throws AgException if expression is malformed or violates validation constraints
     *
     * @since 2.2
     */
    Expression process(AgEntity<?> entity, Expression exp);
}
