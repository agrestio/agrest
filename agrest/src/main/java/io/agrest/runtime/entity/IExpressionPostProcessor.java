package io.agrest.runtime.entity;

import io.agrest.AgException;
import io.agrest.meta.AgEntity;

/**
 * @since 2.2
 */
public interface IExpressionPostProcessor<E> {

    /**
     * Perform any post-processing and cleanup that might be necessary for the successful execution of expression
     * @return Expression that is ready for execution
     * @throws AgException if expression is malformed or violates validation constraints
     *
     * @since 2.2
     */
    E process(AgEntity<?> entity, E exp);
}
