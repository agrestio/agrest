package io.agrest.meta;

import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.ReadFilter;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.resolver.RootDataResolver;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 5.0
 */
public class DefaultEntity<T> implements AgEntity<T> {

    private final String name;
    private final Class<T> type;
    private final boolean _abstract;
    private final Collection<AgEntity<? extends T>> subEntities;
    private final RootDataResolver<T> dataResolver;
    private final ReadFilter<T> readFilter;
    private final CreateAuthorizer<T> createAuthorizer;
    private final UpdateAuthorizer<T> updateAuthorizer;
    private final DeleteAuthorizer<T> deleteAuthorizer;

    // TODO: ensure name uniqueness between all types of properties
    private final Map<String, AgIdPart> ids;
    private final Map<String, AgAttribute> attributes;
    private final Map<String, AgRelationship> relationships;

    // resolved lazily to avoid stack overflow when reading sub entities
    private volatile Map<String, AgAttribute> attributesInHierarchy;
    private volatile Map<String, AgRelationship> relationshipsInHierarchy;

    public DefaultEntity(
            String name,
            Class<T> type,
            boolean _abstract,
            Collection<AgEntity<? extends T>> subEntities,
            Map<String, AgIdPart> ids,
            Map<String, AgAttribute> attributes,
            Map<String, AgRelationship> relationships,
            RootDataResolver<T> dataResolver,
            ReadFilter<T> readFilter,
            CreateAuthorizer<T> createAuthorizer,
            UpdateAuthorizer<T> updateAuthorizer,
            DeleteAuthorizer<T> deleteAuthorizer) {

        this.name = name;
        this.type = type;
        this._abstract = _abstract;
        this.subEntities = subEntities;

        this.ids = ids;
        this.attributes = attributes;
        this.relationships = relationships;

        this.dataResolver = dataResolver;
        this.readFilter = readFilter;
        this.createAuthorizer = createAuthorizer;
        this.updateAuthorizer = updateAuthorizer;
        this.deleteAuthorizer = deleteAuthorizer;
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
    public boolean isAbstract() {
        return _abstract;
    }

    @Override
    public Collection<AgEntity<? extends T>> getSubEntities() {
        return subEntities;
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

    /**
     * @since 5.0
     */
    @Override
    public Collection<AgAttribute> getAttributesInHierarchy() {
        return getOrCreateAttributesInHierarchy().values();
    }

    /**
     * @since 5.0
     */
    @Override
    public AgAttribute getAttributeInHierarchy(String name) {
        return getOrCreateAttributesInHierarchy().get(name);
    }

    protected Map<String, AgAttribute> getOrCreateAttributesInHierarchy() {
        // resolved lazily to avoid stack overflow when reading sub entities
        if (this.attributesInHierarchy == null) {
            this.attributesInHierarchy = collectAllAttributes();
        }
        return attributesInHierarchy;
    }

    /**
     * @since 5.0
     */
    @Override
    public Collection<AgRelationship> getRelationshipsInHierarchy() {
        return getOrCreateRelationshipsInHierarchy().values();
    }

    /**
     * @since 5.0
     */
    @Override
    public AgRelationship getRelationshipInHierarchy(String name) {
        return getOrCreateRelationshipsInHierarchy().get(name);
    }

    protected Map<String, AgRelationship> getOrCreateRelationshipsInHierarchy() {
        // resolved lazily to avoid stack overflow when reading sub entities
        if (this.relationshipsInHierarchy == null) {
            this.relationshipsInHierarchy = collectAllRelationships();
        }
        return relationshipsInHierarchy;
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
    public CreateAuthorizer<T> getCreateAuthorizer() {
        return createAuthorizer;
    }

    @Override
    public UpdateAuthorizer<T> getUpdateAuthorizer() {
        return updateAuthorizer;
    }

    @Override
    public DeleteAuthorizer<T> getDeleteAuthorizer() {
        return deleteAuthorizer;
    }

    @Override
    public String toString() {
        return "DefaultAgEntity[" + getName() + "]";
    }

    protected Map<String, AgAttribute> collectAllAttributes() {

        if (subEntities.isEmpty()) {
            return attributes;
        }

        Map<String, AgAttribute> allAttributes = new HashMap<>();
        for (AgEntity<?> subEntity : subEntities) {
            subEntity.getAttributes().forEach(a -> allAttributes.put(a.getName(), a));
        }

        // add this entity attributes last, in case subclass attributes are redefined (is this even possible?)
        allAttributes.putAll(attributes);

        return allAttributes;
    }

    protected Map<String, AgRelationship> collectAllRelationships() {

        if (subEntities.isEmpty()) {
            return relationships;
        }

        Map<String, AgRelationship> allRelationships = new HashMap<>();
        for (AgEntity<?> subEntity : subEntities) {
            subEntity.getRelationships().forEach(r -> allRelationships.put(r.getName(), r));
        }

        // add this entity relationships last, in case subclass attributes are redefined (is this even possible?)
        allRelationships.putAll(relationships);

        return allRelationships;
    }
}
