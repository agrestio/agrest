package io.agrest.meta;

import io.agrest.filter.CreateFilter;
import io.agrest.filter.DeleteFilter;
import io.agrest.filter.ReadFilter;
import io.agrest.filter.UpdateFilter;
import io.agrest.resolver.RootDataResolver;

import java.util.Collection;
import java.util.Map;

/**
 * @since 1.12
 */
public class DefaultAgEntity<T> implements AgEntity<T> {

    private final String name;
    private final Class<T> type;
    private final RootDataResolver<T> dataResolver;
    private final ReadFilter<T> readFilter;
    private final CreateFilter<T> createFilter;
    private final UpdateFilter<T> updateFilter;
    private final DeleteFilter<T> deleteFilter;

    // TODO: ensure name uniqueness between all types of properties
    private final Map<String, AgIdPart> ids;
    private final Map<String, AgAttribute> attributes;
    private final Map<String, AgRelationship> relationships;

    public DefaultAgEntity(
            String name,
            Class<T> type,
            Map<String, AgIdPart> ids,
            Map<String, AgAttribute> attributes,
            Map<String, AgRelationship> relationships,
            RootDataResolver<T> dataResolver,
            ReadFilter<T> readFilter,
            CreateFilter<T> createFilter,
            UpdateFilter<T> updateFilter,
            DeleteFilter<T> deleteFilter) {

        this.name = name;
        this.type = type;
        this.ids = ids;
        this.attributes = attributes;
        this.relationships = relationships;
        this.dataResolver = dataResolver;
        this.readFilter = readFilter;
        this.createFilter = createFilter;
        this.updateFilter = updateFilter;
        this.deleteFilter = deleteFilter;

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public Collection<AgIdPart> getIdParts() {
        return ids.values();
    }

    @Override
    public AgIdPart getIdPart(String name) {
        return ids.get(name);
    }

    @Override
    public AgRelationship getRelationship(String name) {
        return relationships.get(name);
    }

    @Override
    public AgAttribute getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Collection<AgRelationship> getRelationships() {
        return relationships.values();
    }

    @Override
    public Collection<AgAttribute> getAttributes() {
        return attributes.values();
    }

    @Override
    public RootDataResolver<T> getDataResolver() {
        return dataResolver;
    }

    @Override
    public ReadFilter<T> getReadFilter() {
        return readFilter;
    }

    @Override
    public CreateFilter<T> getCreateFilter() {
        return createFilter;
    }

    @Override
    public UpdateFilter<T> getUpdateFilter() {
        return updateFilter;
    }

    @Override
    public DeleteFilter<T> getDeleteFilter() {
        return deleteFilter;
    }

    @Override
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this)) + "[" + getName() + "]";
    }
}
