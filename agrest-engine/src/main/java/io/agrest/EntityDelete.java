package io.agrest;

import io.agrest.meta.AgEntity;

public class EntityDelete<T> {

    private AgEntity<T> entity;
    private AgObjectId id;

    public EntityDelete(AgEntity<T> entity, AgObjectId id) {
        this.entity = entity;
        this.id = id;
    }

    public AgObjectId getId() {
        return id;
    }

    public AgEntity<T> getEntity() {
        return entity;
    }
}
