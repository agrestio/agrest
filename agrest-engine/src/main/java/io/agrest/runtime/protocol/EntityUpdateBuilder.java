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

    void addIdPart(String idPartName, Object value) {
        currentUpdate.addIdPart(idPartName, value);
    }

    void setAttribute(String attribute, Object value) {
        currentUpdate.setAttribute(attribute, value);
    }

    void relatedId(String relationship, Object relatedId) {
        currentUpdate.addRelatedId(relationship, relatedId);
    }

    void setToOne(String relationship, EntityUpdate<?> update) {
        currentUpdate.setToOne(relationship, update);
    }

    void addToMany(String relationship, EntityUpdate<?> update) {
        currentUpdate.addToMany(relationship, update);
    }

    void endObject() {
        updates.add(currentUpdate);
        currentUpdate = null;
    }

    int getRemainingDepth() {
        return remainingDepth;
    }

    List<EntityUpdate<T>> getUpdates() {
        return updates;
    }
}
