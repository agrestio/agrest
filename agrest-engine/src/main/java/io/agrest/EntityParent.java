package io.agrest;

import java.util.Map;
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

    public EntityParent(Class<P> parentType, Map<String, Object> parentIds, String relationshipFromParent) {
        this(parentType, relationshipFromParent, AgObjectId.ofMap(parentIds));
    }

    public EntityParent(Class<P> parentType, Object parentId, String relationshipFromParent) {
        this(parentType, relationshipFromParent, AgObjectId.of(parentId));
    }

    protected EntityParent(Class<P> parentType, String relationshipFromParent, AgObjectId id) {
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
