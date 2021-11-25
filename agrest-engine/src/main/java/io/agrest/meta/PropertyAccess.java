package io.agrest.meta;

import io.agrest.PathConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A mutable builder of property access rules for a single entity. Used to configure property access in
 * {@link AgEntityOverlay}.
 *
 * @see AgEntityOverlay#readAccess(PropertyAccessRule) 
 * @see AgEntityOverlay#writeAccess(PropertyAccessRule)
 * @since 4.8
 */
public class PropertyAccess {

    // Since we can't resolve the access rules until we are given an entity,
    // we must store them as "filter" operations in the order they were specified.
    // During the resolution phase they will be applied in turn to the entity
    // set of properties
    private final List<Consumer<ExcludeBuilder<?>>> accessFilters;

    public PropertyAccess() {
        this.accessFilters = new ArrayList<>();
    }

    public PropertyAccess id(boolean accessible) {
        return property(PathConstants.ID_PK_ATTRIBUTE, accessible);
    }

    public PropertyAccess attributes(boolean accessible) {
        accessFilters.add(accessible
                ? ExcludeBuilder::includeAllAttributes
                : ExcludeBuilder::excludeAllAttributes);

        return this;
    }

    public PropertyAccess relationships(boolean accessible) {
        accessFilters.add(accessible
                ? ExcludeBuilder::includeAllRelationships
                : ExcludeBuilder::excludeAllRelationships);

        return this;
    }

    public PropertyAccess property(String name, boolean accessible) {
        accessFilters.add(accessible
                ? b -> b.includeProperty(name)
                : b -> b.excludeProperty(name));

        return this;
    }

    <T> Set<String> findInaccessible(AgEntity<T> entity, AgEntityOverlay<T> overlay) {
        if (accessFilters.isEmpty()) {
            return Collections.emptySet();
        }

        ExcludeBuilder<T> builder = new ExcludeBuilder<>(entity, overlay);
        accessFilters.forEach(c -> c.accept(builder));
        return builder.excludes;
    }

    static class ExcludeBuilder<T> {

        final AgEntityOverlay<T> overlay;
        final AgEntity<T> entity;
        final Set<String> excludes;

        ExcludeBuilder(AgEntity<T> entity, AgEntityOverlay<T> overlay) {
            this.overlay = overlay;
            this.entity = entity;
            this.excludes = new HashSet<>();
        }

        void includeAllAttributes() {
            entity.getAttributes().forEach(a -> excludes.remove(a.getName()));
            overlay.getAttributeOverlays().forEach(ao -> excludes.remove(ao.getName()));
        }

        void excludeAllAttributes() {
            entity.getAttributes().forEach(a -> excludes.add(a.getName()));
            overlay.getAttributeOverlays().forEach(ao -> excludes.add(ao.getName()));
        }

        void includeAllRelationships() {
            entity.getRelationships().forEach(r -> excludes.remove(r.getName()));
            overlay.getRelationshipOverlays().forEach(ro -> excludes.remove(ro.getName()));
        }

        void excludeAllRelationships() {
            entity.getRelationships().forEach(r -> excludes.add(r.getName()));
            overlay.getRelationshipOverlays().forEach(ro -> excludes.add(ro.getName()));
        }

        void includeProperty(String name) {
            excludes.remove(name);
        }

        void excludeProperty(String name) {
            excludes.add(name);
        }
    }
}
