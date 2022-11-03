package io.agrest;

import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.protocol.Exp;
import io.agrest.protocol.Sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable view of ResourceEntity with attributes and relationships reduced to the set available for a given subclass.
 *
 * @since 5.0
 */
public class FilteredResourceEntity<T> implements ResourceEntity<T> {

    private final AgEntity<T> agEntity;
    private final ResourceEntity<? super T> delegate;

    public FilteredResourceEntity(AgEntity<T> agEntity, ResourceEntity<? super T> delegate) {

        if (!delegate.getType().isAssignableFrom(agEntity.getType())) {
            throw AgException.internalServerError("%s is not a sub-entity of %s", agEntity.getName(), delegate.getName());
        }

        this.agEntity = agEntity;
        this.delegate = delegate;
    }

    @Override
    public AgEntity<T> getAgEntity() {
        return agEntity;
    }

    @Override
    public <ST extends T> ResourceEntity<ST> asSubEntity(AgEntity<ST> subEntity) {
        return delegate.asSubEntity(subEntity);
    }

    @Override
    public AgAttribute getAttribute(String name) {
        // filter delegate attributes to only include those in our root entity
        return agEntity.getAttribute(name) != null ? delegate.getAttribute(name) : null;
    }

    @Override
    public Map<String, AgAttribute> getAttributes() {
        // filter delegate attributes to only include those in our root entity
        Map<String, AgAttribute> filtered = new HashMap<>();
        for (Map.Entry<String, AgAttribute> e : delegate.getAttributes().entrySet()) {
            if (agEntity.getAttribute(e.getKey()) != null) {
                filtered.put(e.getKey(), e.getValue());
            }
        }

        return filtered;
    }

    @Override
    public boolean isDefaultAttribute(String name) {
        // filter default attribute names
        return agEntity.getAttribute(name) != null && delegate.isDefaultAttribute(name);
    }

    @Override
    public RelatedResourceEntity<?> getChild(String name) {
        // filter delegate relationships to only include those in our root entity
        return agEntity.getRelationship(name) != null ? delegate.getChild(name) : null;
    }

    @Override
    public Map<String, RelatedResourceEntity<?>> getChildren() {
        // filter delegate attributes to only include those in our root entity
        Map<String, RelatedResourceEntity<?>> filtered = new HashMap<>();
        for (Map.Entry<String, RelatedResourceEntity<?>> e : delegate.getChildren().entrySet()) {
            if (agEntity.getRelationship(e.getKey()) != null) {
                filtered.put(e.getKey(), e.getValue());
            }
        }

        return filtered;
    }

    @Override
    public Exp getExp() {
        return delegate.getExp();
    }

    @Override
    public void andExp(Exp exp) {
        delegate.andExp(exp);
    }

    @Override
    public List<Sort> getOrderings() {
        return delegate.getOrderings();
    }

    @Override
    public void addAttribute(AgAttribute attribute, boolean isDefault) {
        throw new UnsupportedOperationException("This entity view is read-only");
    }

    @Override
    public AgAttribute removeAttribute(String name) {
        throw new UnsupportedOperationException("This entity view is read-only");
    }

    @Override
    public RelatedResourceEntity<?> removeChild(String name) {
        throw new UnsupportedOperationException("This entity view is read-only");
    }

    @Override
    public boolean isIdIncluded() {
        return delegate.isIdIncluded();
    }

    @Override
    public ResourceEntity<T> includeId(boolean include) {
        throw new UnsupportedOperationException("This entity view is read-only");
    }

    @Override
    public ResourceEntity<T> includeId() {
        throw new UnsupportedOperationException("This entity view is read-only");
    }

    @Override
    public ResourceEntity<T> excludeId() {
        throw new UnsupportedOperationException("This entity view is read-only");
    }

    @Override
    public ResourceEntity<?> getMapBy() {
        return delegate.getMapBy();
    }

    @Override
    public ResourceEntity<T> mapBy(ResourceEntity<?> mapBy) {
        throw new UnsupportedOperationException("This entity view is read-only");
    }

    @Override
    public int getStart() {
        return delegate.getStart();
    }

    @Override
    public void setStart(int start) {
        throw new UnsupportedOperationException("This entity view is read-only");
    }

    @Override
    public int getLimit() {
        return delegate.getLimit();
    }

    @Override
    public void setLimit(int limit) {
        throw new UnsupportedOperationException("This entity view is read-only");
    }

    @Override
    public boolean isFiltered() {
        return delegate.isFiltered();
    }

    @Override
    public <P> P getProperty(String name) {
        return delegate.getProperty(name);
    }

    @Override
    public void setProperty(String name, Object value) {
        throw new UnsupportedOperationException("This entity view is read-only");
    }
}
