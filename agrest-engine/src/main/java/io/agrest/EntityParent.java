package io.agrest;

import io.agrest.meta.AgEntity;

import java.util.Objects;

/**
 * Represents a parent in a relationship request.
 *
 * @since 1.4
 */
public class EntityParent<P> {

    private final AgEntity<P> entity;
    private final String relationship;
    private final AgObjectId id;

    public EntityParent(AgEntity<P> entity, AgObjectId id, String relationshipFromParent) {
        this.entity = Objects.requireNonNull(entity);
        this.relationship = Objects.requireNonNull(relationshipFromParent);
        this.id = Objects.requireNonNull(id);
    }

    /**
     * @since 5.0
     */
    public AgEntity<P> getEntity() {
        return entity;
    }

    public AgObjectId getId() {
        return id;
    }

    public String getRelationship() {
        return relationship;
    }
}
