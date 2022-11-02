
package io.agrest.meta;

import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.ReadFilter;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.resolver.RootDataResolver;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * @since 5.0
 */
public class LazyEntity<T> extends BaseLazyEntity<T, AgEntity<T>> implements AgEntity<T> {

    private final Class<T> type;

    public LazyEntity(Class<T> type, Supplier<AgEntity<T>> delegateSupplier) {
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
    public boolean isAbstract() {
        return getDelegate().isAbstract();
    }

    @Override
    public Collection<AgEntity<? extends T>> getSubEntities() {
        return getDelegate().getSubEntities();
    }

    @Override
    public Collection<AgIdPart> getIdParts() {
        return getDelegate().getIdParts();
    }

    @Override
    public AgIdPart getIdPart(String name) {
        return getDelegate().getIdPart(name);
    }

    @Override
    public Collection<AgAttribute> getAttributes() {
        return getDelegate().getAttributes();
    }

    /**
     * @since 5.0
     */
    @Override
    public Collection<AgAttribute> getAttributesInHierarchy() {
        return getDelegate().getAttributesInHierarchy();
    }

    @Override
    public AgAttribute getAttribute(String name) {
        return getDelegate().getAttribute(name);
    }

    @Override
    public AgAttribute getAttributeInHierarchy(String name) {
        return getDelegate().getAttributeInHierarchy(name);
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
    public AgRelationship getRelationshipInHierarchy(String name) {
        return getDelegate().getRelationshipInHierarchy(name);
    }

    @Override
    public Collection<AgRelationship> getRelationshipsInHierarchy() {
        return getDelegate().getRelationshipsInHierarchy();
    }

    @Override
    public RootDataResolver<T> getDataResolver() {
        return getDelegate().getDataResolver();
    }

    @Override
    public ReadFilter<T> getReadFilter() {
        return getDelegate().getReadFilter();
    }

    @Override
    public CreateAuthorizer<T> getCreateAuthorizer() {
        return getDelegate().getCreateAuthorizer();
    }

    @Override
    public UpdateAuthorizer<T> getUpdateAuthorizer() {
        return getDelegate().getUpdateAuthorizer();
    }

    @Override
    public DeleteAuthorizer<T> getDeleteAuthorizer() {
        return getDelegate().getDeleteAuthorizer();
    }

    @Override
    public String toString() {
        return "LazyAgEntity[" + type.getSimpleName() + "]";
    }
}
