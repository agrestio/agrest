package io.agrest.runtime;

import io.agrest.id.AgObjectId;

import java.util.Objects;

/**
 * Represents a parent in a relationship request.
 *
 * @since 5.0
 */
public class EntityParent<P> {

    private final Class<P> type;
    private final String relationship;
    private final AgObjectId id;

    public EntityParent(Class<P> type, AgObjectId id, String relationshipFromParent) {
        this.type = Objects.requireNonNull(type);
        this.relationship = Objects.requireNonNull(relationshipFromParent);
        this.id = Objects.requireNonNull(id);
    }

    public Class<P> getType() {
        return type;
    }

    public AgObjectId getId() {
        return id;
    }

    public String getRelationship() {
        return relationship;
    }
}
