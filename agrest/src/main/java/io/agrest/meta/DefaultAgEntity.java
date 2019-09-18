package io.agrest.meta;

import io.agrest.property.IdReader;
import io.agrest.resolver.RootDataResolver;

import java.util.Collection;
import java.util.Map;

/**
 * @since 1.12
 */
public class DefaultAgEntity<T> implements AgEntity<T> {

    private String name;
    private Class<T> type;
    private IdReader idReader;
    private RootDataResolver<T> dataResolver;

    // TODO: ensure name uniqueness between all types of properties
    private Map<String, AgAttribute> ids;
    private Map<String, AgAttribute> attributes;
    private Map<String, AgRelationship> relationships;

    public DefaultAgEntity(
            String name,
            Class<T> type,
            Map<String, AgAttribute> ids,
            Map<String, AgAttribute> attributes,
            Map<String, AgRelationship> relationships,
            IdReader idReader,
            RootDataResolver<T> dataResolver) {

        this.name = name;
        this.type = type;
        this.ids = ids;
        this.attributes = attributes;
        this.relationships = relationships;
        this.idReader = idReader;
        this.dataResolver = dataResolver;
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
    public IdReader getIdReader() {
        return idReader;
    }

    @Override
    public Collection<AgAttribute> getIds() {
        return ids.values();
    }

    @Override
    public AgAttribute getIdAttribute(String name) {
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
    public String toString() {
        return new StringBuilder(getClass().getName()).append("@")
                .append(Integer.toHexString(System.identityHashCode(this))).append("[").append(getName()).append("]")
                .toString();
    }
}
