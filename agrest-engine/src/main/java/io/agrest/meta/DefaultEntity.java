package io.agrest.meta;

import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.ReadFilter;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.resolver.RootDataResolver;

import java.util.Collection;
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
        return overlays.isEmpty()
                ? this
                : resolveOverlayHierarchy(schema, this, null, overlays);
    }

    private static <T> AgEntity<T> resolveOverlayHierarchy(
            AgSchema schema,
            AgEntity<T> entity,
            AgEntityOverlay<? super T> superOverlay,
            Map<Class<?>, AgEntityOverlay<?>> overlays) {

        AgEntityOverlay<T> overlay = (AgEntityOverlay<T>) overlays.get(entity.getType());
        AgEntityOverlay<T> mergedOverlay = combineOverlays(entity.getType(), overlay, superOverlay);

        if (entity.getSubEntities().isEmpty()) {
            return entity.resolveOverlay(schema, mergedOverlay);
        }

        boolean subEntitiesOverlaid = false;

        Set<AgEntity<? extends T>> subsOverlaid = new HashSet<>();
        for (AgEntity<? extends T> sub : entity.getSubEntities()) {
            AgEntity<? extends T> subOverlaid = resolveOverlayHierarchy(schema, sub, mergedOverlay, overlays);

            subsOverlaid.add(subOverlaid);
            subEntitiesOverlaid = subEntitiesOverlaid || subOverlaid != sub;
        }

        // force overlaid entity creation if any of the subs got overlaid, even if this entity is not explicitly
        // overlaid

        if (subEntitiesOverlaid) {
            AgEntityOverlay<T> rootOverlay = mergedOverlay != null ? mergedOverlay : AgEntity.overlay(entity.getType());
            return rootOverlay.resolve(schema, entity, subsOverlaid);
        } else {
            return mergedOverlay != null && !mergedOverlay.isEmpty()
                    ? mergedOverlay.resolve(schema, entity, entity.getSubEntities())
                    : entity;
        }
    }

    private static <T> AgEntityOverlay<T> combineOverlays(
            Class<T> type,
            AgEntityOverlay<T> overlay,
            AgEntityOverlay<? super T> superOverlay) {

        if (overlay != null) {
            return overlay.mergeSuper(superOverlay);
        } else if (superOverlay != null) {
            return superOverlay.clone(type);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "DefaultAgEntity[" + getName() + "]";
    }
}
