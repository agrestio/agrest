package io.agrest.meta;

import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.ReadFilter;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.resolver.RootDataResolver;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    @Override
    public Collection<AgAttribute> getAttributesInHierarchy() {
        return getOrCreateAttributesInHierarchy().values();
    }

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

    @Override
    public Collection<AgRelationship> getRelationshipsInHierarchy() {
        return getOrCreateRelationshipsInHierarchy().values();
    }

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
    public AgEntity<T> resolveOverlayHierarchy(AgSchema schema, Map<Class<?>, AgEntityOverlay<?>> overlays) {

        AgEntityOverlay<T> overlay = (AgEntityOverlay<T>) overlays.get(getType());

        if (getSubEntities().isEmpty()) {
            return resolveOverlay(schema, overlay);
        }

        boolean subEntitiesOverlaid = false;

        Set<AgEntity<? extends T>> subsOverlaid = new HashSet<>();
        for (AgEntity<? extends T> sub : getSubEntities()) {
            AgEntity<? extends T> subOverlaid = sub.resolveOverlayHierarchy(schema, overlays);

            subsOverlaid.add(subOverlaid);
            subEntitiesOverlaid = subEntitiesOverlaid || subOverlaid != sub;
        }

        // force overlaid entity creation if any of the subs got overlaid, even if this entity is not explicitly
        // overlaid

        if (subEntitiesOverlaid) {
            AgEntityOverlay<T> rootOverlay = overlay != null ? overlay : AgEntity.overlay(getType());
            return rootOverlay.resolve(schema, this, subsOverlaid);
        }
        else {
            return overlay != null && !overlay.isEmpty() ? overlay.resolve(schema, this, getSubEntities()) : this;
        }
    }

    @Override
    public String toString() {
        return "DefaultAgEntity[" + getName() + "]";
    }

    protected Map<String, AgAttribute> collectAllAttributes() {
        return subEntities.isEmpty() ? attributes : collectAllAttributes(new HashMap<>(), this);
    }

    protected Map<String, AgAttribute> collectAllAttributes(Map<String, AgAttribute> collectTo, AgEntity<?> entity) {
        // on the off-chance that sub-entities redefine our attributes, add subclasses first, and then add ours
        // to override everything for this entity
        entity.getSubEntities().forEach(se -> collectAllAttributes(collectTo, se));
        entity.getAttributes().forEach(a -> collectTo.put(a.getName(), a));

        return collectTo;
    }

    protected Map<String, AgRelationship> collectAllRelationships() {
        return subEntities.isEmpty() ? relationships : collectAllRelationships(new HashMap<>(), this);
    }

    protected Map<String, AgRelationship> collectAllRelationships(Map<String, AgRelationship> collectTo, AgEntity<?> entity) {
        // on the off-chance that sub-entities redefine our relationships, add subclasses first, and then add ours
        // to override everything for this entity
        entity.getSubEntities().forEach(se -> collectAllRelationships(collectTo, se));
        entity.getRelationships().forEach(r -> collectTo.put(r.getName(), r));

        return collectTo;
    }
}
