package com.nhl.link.rest;

import com.nhl.link.rest.meta.LrEntity;

public class EntityDelete<T> {

    private LrEntity<T> entity;
    private LrObjectId id;

    public EntityDelete(LrEntity<T> entity, LrObjectId id) {
        this.entity = entity;
        this.id = id;
    }

    public LrObjectId getId() {
        return id;
    }

    public LrEntity<T> getEntity() {
        return entity;
    }
}
