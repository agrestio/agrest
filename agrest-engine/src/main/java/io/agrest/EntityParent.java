package io.agrest;

import java.util.Objects;

/**
 * Represents a parent in a relationship request.
 *
 * @since 1.4
 */
public class EntityParent<P> {

    private final Class<P> type;
    private final String relationship;
    private final AgObjectId id;

    public EntityParent(Class<P> parentType, AgObjectId id, String relationshipFromParent) {
        this.type = Objects.requireNonNull(parentType);
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
