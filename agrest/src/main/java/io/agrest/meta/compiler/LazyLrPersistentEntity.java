package io.agrest.meta.compiler;

import io.agrest.meta.LrAttribute;
import io.agrest.meta.LrPersistentAttribute;
import io.agrest.meta.LrPersistentEntity;
import io.agrest.meta.LrRelationship;
import org.apache.cayenne.map.ObjEntity;

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
    public ObjEntity getObjEntity() {
        return getDelegate().getObjEntity();
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
