package com.nhl.link.rest.runtime.processor.unrelate;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.processor.BaseProcessingContext;

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
