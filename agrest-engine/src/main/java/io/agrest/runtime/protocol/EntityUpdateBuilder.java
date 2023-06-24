package io.agrest.runtime.protocol;

import io.agrest.EntityUpdate;
import io.agrest.meta.AgEntity;

import java.util.ArrayList;
import java.util.Collection;

class EntityUpdateBuilder<T> {

    private final AgEntity<T> entity;
    private final Collection<EntityUpdate<T>> updates;

    private EntityUpdate<T> currentUpdate;

    EntityUpdateBuilder(AgEntity<T> entity) {
        this.entity = entity;
        this.updates = new ArrayList<>();
    }

    void beginObject() {
        currentUpdate = new EntityUpdate<>(entity);
    }

    void idPart(String name, Object value) {
        currentUpdate.getOrCreateId().put(name, value);
    }

    void attribute(String name, Object value) {
        currentUpdate.getValues().put(name, value);
    }

    void relationship(String name, Object relatedId) {
        currentUpdate.addRelatedId(name, relatedId);
    }

    void endObject() {
        updates.add(currentUpdate);
        currentUpdate = null;
    }

    Collection<EntityUpdate<T>> getUpdates() {
        return updates;
    }
}
