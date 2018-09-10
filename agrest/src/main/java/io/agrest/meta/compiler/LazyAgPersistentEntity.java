package io.agrest.meta.compiler;

import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgPersistentAttribute;
import io.agrest.meta.AgPersistentEntity;
import io.agrest.meta.AgRelationship;
import org.apache.cayenne.map.ObjEntity;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * @since 2.0
 */
public class LazyAgPersistentEntity<T> extends BaseLazyAgEntity<T, AgPersistentEntity<T>> implements AgPersistentEntity<T> {

    private Class<T> type;

    public LazyAgPersistentEntity(Class<T> type, Supplier<AgPersistentEntity<T>> delegateSupplier) {
        super(delegateSupplier);
        this.type = type;
    }

    @Override
    public ObjEntity getObjEntity() {
        return getDelegate().getObjEntity();
    }

    @Override
    public AgPersistentAttribute getPersistentAttribute(String name) {
        return getDelegate().getPersistentAttribute(name);
    }

    @Override
    public Collection<AgPersistentAttribute> getPersistentAttributes() {
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
}
