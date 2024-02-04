package io.agrest.runtime.constraints;

import io.agrest.runtime.processor.select.SelectContext;

/**
 * @since 5.0
 */
public class SelectConstraints {

    public <T> void apply(SelectContext<T> context) {
        ReadPropertyFilter.apply(context.getEntity());
        SizeFilter.apply(context.getEntity(), context.getSizeConstraints());
    }
}
