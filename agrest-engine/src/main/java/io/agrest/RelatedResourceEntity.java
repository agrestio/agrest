package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.resolver.RelatedDataResolver;

/**
 * @param <T>
 * @since 5.0
 */
public abstract class RelatedResourceEntity<T> extends ResourceEntity<T> {

    private final ResourceEntity<?> parent;
    private final AgRelationship incoming;
    private RelatedDataResolver<T> resolver;

    public RelatedResourceEntity(
            AgEntity<T> agEntity,
            ResourceEntity<?> parent,
            AgRelationship incoming) {

        super(agEntity);
        this.incoming = incoming;
        this.parent = parent;
        this.resolver = (RelatedDataResolver<T>) incoming.getDataResolver();
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

    public RelatedDataResolver<T> getResolver() {
        return resolver;
    }

    public void setResolver(RelatedDataResolver<T> resolver) {
        this.resolver = resolver;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + getIncoming().getName() + '>' + getName() + ']';
    }
}
