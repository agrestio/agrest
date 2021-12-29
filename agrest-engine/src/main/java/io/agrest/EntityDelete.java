package io.agrest;

import io.agrest.meta.AgEntity;

/**
 * @deprecated since 5.0 as DELETE HTTP method has no body.
 */
@Deprecated
public class EntityDelete<T> {

    private final AgEntity<T> entity;
    private final AgObjectId id;

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
