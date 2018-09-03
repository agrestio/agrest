package io.agrest.meta.compiler;

import io.agrest.meta.LrAttribute;
import io.agrest.meta.LrEntity;
import io.agrest.meta.LrRelationship;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * @since 2.0
 */
public class LazyLrEntity<T> extends BaseLazyLrEntity<T, LrEntity<T>> implements LrEntity<T> {

    private Class<T> type;

    public LazyLrEntity(Class<T> type, Supplier<LrEntity<T>> delegateSupplier) {
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
