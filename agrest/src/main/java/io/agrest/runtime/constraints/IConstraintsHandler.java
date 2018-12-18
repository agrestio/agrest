package io.agrest.runtime.constraints;

import io.agrest.ResourceEntity;
import io.agrest.SizeConstraints;
import io.agrest.constraints.Constraint;
import io.agrest.runtime.processor.update.UpdateContext;

/**
 * @since 1.3
 */
public interface IConstraintsHandler {

    /**
     * Applies constraints to the {@link ResourceEntity}, potentially filtering out some properties from the response.
     */
    <T, E> void constrainResponse(ResourceEntity<T, E> entity, SizeConstraints sizeConstraints, Constraint<T, E> readConstraints);

    /**
     * Applies constraints to the {@link UpdateContext}, potentially filtering out updates for certain properties.
     */
    <T, E> void constrainUpdate(UpdateContext<T, E> context, Constraint<T, E> writeConstraints);
}
