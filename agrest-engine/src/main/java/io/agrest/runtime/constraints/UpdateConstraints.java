package io.agrest.runtime.constraints;

import io.agrest.runtime.processor.update.UpdateContext;

/**
 * @since 5.0
 */
public class UpdateConstraints {

    public <T> void apply(UpdateContext<T> context) {
        WriteConstraints.apply(context.getEntity(), context.getUpdates());
        ReadConstraints.apply(context.getEntity());
    }
}
