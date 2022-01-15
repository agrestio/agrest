package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.resolver.NestedDataResolver;

/**
 * @param <T>
 * @since 3.4
 */
public abstract class NestedResourceEntity<T> extends ResourceEntity<T> {

    private final ResourceEntity<?> parent;
    private final AgRelationship incoming;
    private NestedDataResolver<T> resolver;

    public NestedResourceEntity(
            AgEntity<T> agEntity,
            // TODO: Instead of AgRelationship introduce some kind of RERelationship that has references to both parent and child
            ResourceEntity<?> parent,
            AgRelationship incoming) {

        super(agEntity);
        this.incoming = incoming;
        this.parent = parent;
        this.resolver = (NestedDataResolver<T>) incoming.getResolver();
    }

    /**
     * @since 5.0
     */
    public abstract void addData(AgObjectId parentId, T object);

    /**
     * @deprecated since 5.0 in favor of {@link #addData(AgObjectId, Object)}
     */
    @Deprecated
    public void addResult(AgObjectId parentId, T object) {
        addData(parentId, object);
    }

    public ResourceEntity<?> getParent() {
        return parent;
    }

    public AgRelationship getIncoming() {
        return incoming;
    }

    public NestedDataResolver<T> getResolver() {
        return resolver;
    }

    public void setResolver(NestedDataResolver<T> resolver) {
        this.resolver = resolver;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + getIncoming().getName() + '>' + getName() + ']';
    }
}
