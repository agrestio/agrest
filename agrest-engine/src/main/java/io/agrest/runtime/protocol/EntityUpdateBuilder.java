package io.agrest.runtime.protocol;

import io.agrest.EntityUpdate;
import io.agrest.meta.AgEntity;

import java.util.ArrayList;
import java.util.List;

class EntityUpdateBuilder<T> {

    private final AgEntity<T> entity;
    private final int remainingDepth;
    private final List<EntityUpdate<T>> updates;

    private EntityUpdate<T> currentUpdate;

    EntityUpdateBuilder(AgEntity<T> entity, int remainingDepth) {
        this.entity = entity;
        this.remainingDepth = remainingDepth;
        this.updates = new ArrayList<>();
    }

    void beginObject() {
        currentUpdate = new EntityUpdate<>(entity);
    }

    void endObject() {
        updates.add(currentUpdate);
        currentUpdate = null;
    }

    EntityUpdate<T> getCurrentUpdate() {
        return currentUpdate;
    }

    int getRemainingDepth() {
        return remainingDepth;
    }

    List<EntityUpdate<T>> getUpdates() {
        return updates;
    }
}
