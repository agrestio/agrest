package io.agrest.meta;

import io.agrest.filter.ObjectFilter;
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
    private final ObjectFilter readableObjectFilter;
    private final ObjectFilter writableObjectFilter;

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
            ObjectFilter readableObjectFilter,
            ObjectFilter writableObjectFilter) {

        this.name = name;
        this.type = type;
        this.ids = ids;
        this.attributes = attributes;
        this.relationships = relationships;
        this.dataResolver = dataResolver;
        this.readableObjectFilter = readableObjectFilter;
        this.writableObjectFilter = writableObjectFilter;
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
    public ObjectFilter getReadableObjectFilter() {
        return readableObjectFilter;
    }

    @Override
    public ObjectFilter getWritableObjectFilter() {
        return writableObjectFilter;
    }

    @Override
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this)) + "[" + getName() + "]";
    }
}
