package io.agrest.runtime.constraints;

import io.agrest.ResourceEntity;
import io.agrest.SizeConstraints;
import io.agrest.runtime.processor.update.UpdateContext;

/**
 * @since 1.3
 */
public interface IConstraintsHandler {

    /**
     * Applies constraints to the {@link ResourceEntity}, potentially filtering out some properties from the response.
     */
    <T> void constrainResponse(ResourceEntity<T> entity, SizeConstraints sizeConstraints);

    /**
     * Applies constraints to the {@link UpdateContext}, potentially filtering out updates for certain properties.
     */
    <T> void constrainUpdate(UpdateContext<T> context);
}
