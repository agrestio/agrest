package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.runtime.processor.select.SelectContext;

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

    public abstract void addResult(AgObjectId parentId, T object);

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

    /**
     * @deprecated since 4.8 use "getResolver().onParentQueryAssembled()"
     */
    @Deprecated
    public void onParentQueryAssembled(SelectContext<?> context) {
        resolver.onParentQueryAssembled(this, context);
    }

    /**
     * @deprecated since 4.8 use "getResolver().onParentDataResolved()"
     */
    @Deprecated
    public void onParentDataResolved(Iterable<?> parentData, SelectContext<?> context) {
        resolver.onParentDataResolved(this, parentData, context);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + getIncoming().getName() + '>' + getName() + ']';
    }
}
