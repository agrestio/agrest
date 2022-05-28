package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.meta.AgSchema;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.AgRelationship;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * {@link ResourceEntityTreeBuilder} with additional tracking of "non-phantom" entities that will later need to be
 * inflated with default attributes.
 *
 * @since 3.4
 */
public class PhantomTrackingResourceEntityTreeBuilder extends ResourceEntityTreeBuilder {

    private Set<ResourceEntity<?>> nonPhantomEntities;

    public PhantomTrackingResourceEntityTreeBuilder(
            ResourceEntity<?> rootEntity,
            AgSchema schema,
            Map<Class<?>, AgEntityOverlay<?>> entityOverlays) {

        super(rootEntity, schema, entityOverlays);
        this.nonPhantomEntities = new HashSet<>();

        // "root" always a candidate for defaults, as it is included implicitly
        this.nonPhantomEntities.add(rootEntity);
    }

    /**
     * @return a subset of entities among those that were "inflated" that are "non-phantom". I.e. those that are
     * included explicitly and would require a default attribute set if no explicit attributes are present.
     */
    public Set<ResourceEntity<?>> nonPhantomEntities() {
        return nonPhantomEntities;
    }

    @Override
    protected ResourceEntity<?> inflateChild(ResourceEntity<?> parentEntity, AgRelationship relationship, String childPath) {
        ResourceEntity childEntity = super.inflateChild(parentEntity, relationship, childPath);

        if (childPath == null) {
            // explicit relationship "include" may need defaults
            nonPhantomEntities.add(childEntity);
        }

        return childEntity;
    }
}
