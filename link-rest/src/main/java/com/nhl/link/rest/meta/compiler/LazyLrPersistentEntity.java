package com.nhl.link.rest.meta.compiler;

import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.meta.LrPersistentEntity;
import com.nhl.link.rest.meta.LrRelationship;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * @since 2.0
 */
public class LazyLrPersistentEntity<T> extends BaseLazyLrEntity<T, LrPersistentEntity<T>> implements LrPersistentEntity<T> {

    private Class<T> type;

    public LazyLrPersistentEntity(Class<T> type, Supplier<LrPersistentEntity<T>> delegateSupplier) {
        super(delegateSupplier);
        this.type = type;
    }

    @Override
    public LrPersistentAttribute getPersistentAttribute(String name) {
        return getDelegate().getPersistentAttribute(name);
    }

    @Override
    public Collection<LrPersistentAttribute> getPersistentAttributes() {
        return getDelegate().getPersistentAttributes();
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
    public Collection<LrAttribute> getIds() {
        return getDelegate().getIds();
    }

    @Override
    public Collection<LrAttribute> getAttributes() {
        return getDelegate().getAttributes();
    }

    @Override
    public LrAttribute getAttribute(String name) {
        return getDelegate().getAttribute(name);
    }

    @Override
    public Collection<LrRelationship> getRelationships() {
        return getDelegate().getRelationships();
    }

    @Override
    public LrRelationship getRelationship(String name) {
        return getDelegate().getRelationship(name);
    }
}
