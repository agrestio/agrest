package io.agrest.runtime.processor.update;

import io.agrest.EntityUpdate;

import java.util.Collection;
import java.util.Objects;

/**
 * @since 4.8
 */
public class UpdateOperation<T> {

    private final UpdateOperationType type;
    private final T object;
    private final Collection<EntityUpdate<T>> updates;

    public UpdateOperation(UpdateOperationType type, T object, Collection<EntityUpdate<T>> updates) {
        this.type = Objects.requireNonNull(type);
        this.object = object;
        this.updates = updates;
    }

    public UpdateOperationType getType() {
        return type;
    }

    public T getObject() {
        return object;
    }

    public Collection<EntityUpdate<T>> getUpdates() {
        return updates;
    }
}
