package io.agrest.meta.compiler;

import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * @since 2.0
 */
public class LazyAgEntity<T> extends BaseLazyAgEntity<T, AgEntity<T>> implements AgEntity<T> {

    private Class<T> type;

    public LazyAgEntity(Class<T> type, Supplier<AgEntity<T>> delegateSupplier) {
        super(delegateSupplier);
        this.type = type;
    }

    @Override
    public String getName() {
        return getDelegate().getName();
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public Collection<AgAttribute> getIds() {
        return getDelegate().getIds();
    }

    @Override
    public Collection<AgAttribute> getAttributes() {
        return getDelegate().getAttributes();
    }

    @Override
    public AgAttribute getAttribute(String name) {
        return getDelegate().getAttribute(name);
    }

    @Override
    public Collection<AgRelationship> getRelationships() {
        return getDelegate().getRelationships();
    }

    @Override
    public AgRelationship getRelationship(String name) {
        return getDelegate().getRelationship(name);
    }

    @Override
    public AgRelationship getRelationship(AgEntity entity) {
        return getDelegate().getRelationship(entity);
    }
}
