package io.agrest.runtime.processor.update;

import io.agrest.EntityUpdate;

import java.util.Collection;
import java.util.Objects;

/**
 * Per-object create, update or delete operation. A list of operations is created by Agrest during the
 * {@link io.agrest.UpdateStage#MAP_CHANGES} stage and is available from the execution {@link UpdateContext}.
 *
 * @since 4.8
 */
public class ChangeOperation<T> {

    private final ChangeOperationType type;
    private final T object;
    private final Collection<EntityUpdate<T>> updates;

    public ChangeOperation(ChangeOperationType type, T object, Collection<EntityUpdate<T>> updates) {
        this.type = Objects.requireNonNull(type);
        this.object = object;
        this.updates = updates;
    }

    public ChangeOperationType getType() {
        return type;
    }

    public T getObject() {
        return object;
    }

    public Collection<EntityUpdate<T>> getUpdates() {
        return updates;
    }
}
