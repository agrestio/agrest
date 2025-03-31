package io.agrest.runtime.constraints;

import io.agrest.runtime.processor.update.UpdateContext;

/**
 * @since 5.0
 */
public class UpdateConstraints {

    public <T> void apply(UpdateContext<T> context) {
        WritePropertyFilter.apply(context.getEntity(), context.getUpdates());
        ReadPropertyFilter.apply(context.getEntity());
    }
}
