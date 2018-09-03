package io.agrest.runtime.processor.unrelate;

import io.agrest.EntityParent;
import io.agrest.processor.BaseProcessingContext;

/**
 * @since 1.16
 */
public class UnrelateContext<T> extends BaseProcessingContext<T> {

    private EntityParent<?> parent;
    private Object id;

    public UnrelateContext(Class<T> type, EntityParent<?> parent) {
        super(type);
        this.parent = parent;
    }

    public UnrelateContext(Class<T> type, EntityParent<?> parent, Object id) {
        this(type, parent);
        this.id = id;
    }

    public EntityParent<?> getParent() {
        return parent;
    }

    public Object getId() {
        return id;
    }
}
