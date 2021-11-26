package io.agrest.filter;

import io.agrest.PathConstants;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A mutable builder of property access constraints for a single entity. Used to configure property access in
 * {@link AgEntityOverlay}.
 *
 * @see PropertyFilter#apply(PropertyFilteringRulesBuilder)
 * @since 4.8
 */
public class PropertyFilteringRulesBuilder {

    // Since we can't resolve the access rules until we are given an entity,
    // we must store them as "filter" operations in the order they were specified.
    // During the resolution phase they will be applied in turn to the entity
    // set of properties
    private final List<Consumer<ExcludeBuilder<?>>> accessFilters;

    public PropertyFilteringRulesBuilder() {
        this.accessFilters = new ArrayList<>();
    }

    /**
     * Creates a rule to block access to all properties (ids, attributes, relationships) from the entity model.
     */
    public PropertyFilteringRulesBuilder empty() {
        accessFilters.add(ExcludeBuilder::excludeEverything);
        return this;
    }

    /**
     * Creates a rule to block access to all properties, but allows access to id
     */
    public PropertyFilteringRulesBuilder idOnly() {
        accessFilters.add(ExcludeBuilder::includeIdOnly);
        return this;
    }

    /**
     * Sets an access rule for the id property.
     */
    public PropertyFilteringRulesBuilder id(boolean accessible) {
        return property(PathConstants.ID_PK_ATTRIBUTE, accessible);
    }

    /**
     * Sets an access rule for all attribute properties.
     */
    public PropertyFilteringRulesBuilder attributes(boolean accessible) {
        accessFilters.add(accessible
                ? ExcludeBuilder::includeAllAttributes
                : ExcludeBuilder::excludeAllAttributes);

        return this;
    }

    /**
     * Sets an access rule for all relationship properties.
     */
    public PropertyFilteringRulesBuilder relationships(boolean accessible) {
        accessFilters.add(accessible
                ? ExcludeBuilder::includeAllRelationships
                : ExcludeBuilder::excludeAllRelationships);

        return this;
    }

    /**
     * Sets am access rule for a given named property, that can be an attribute, a relationship or an id.
     */
    public PropertyFilteringRulesBuilder property(String name, boolean accessible) {
        accessFilters.add(accessible
                ? b -> b.includeProperty(name)
                : b -> b.excludeProperty(name));

        return this;
    }

    /**
     * A build method that returns a set of inaccessible properties based on the builder rules configured previously
     * by the user.
     */
    public <T> Set<String> resolveInaccessible(AgEntity<T> entity, AgEntityOverlay<T> overlay) {
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

        void excludeEverything() {
            excludeAllAttributes();
            excludeAllRelationships();
            excludeProperty(PathConstants.ID_PK_ATTRIBUTE);
        }

        void includeIdOnly() {
            excludeAllAttributes();
            excludeAllRelationships();
            includeProperty(PathConstants.ID_PK_ATTRIBUTE);
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
