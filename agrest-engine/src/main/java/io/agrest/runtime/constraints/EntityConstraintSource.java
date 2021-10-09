package io.agrest.runtime.constraints;

import io.agrest.EntityConstraint;
import io.agrest.meta.AgEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @since 1.6
 */
abstract class EntityConstraintSource {

    private ConcurrentMap<String, EntityConstraint> constraints;

    EntityConstraintSource(ConcurrentMap<String, EntityConstraint> constraints) {
        this.constraints = constraints;
    }

    EntityConstraint getOrCreate(AgEntity<?> entity) {
        return constraints.computeIfAbsent(entity.getName(), n -> create(entity));
    }

    protected abstract AccessibleProperties findAccessible(AgEntity<?> entity);

    private EntityConstraint create(AgEntity<?> entity) {
        AccessibleProperties ap = findAccessible(entity);

        boolean allowsId = ap.idParts.size() == entity.getIdParts().size();
        boolean allowsAllAttributes = ap.attributes.size() == entity.getAttributes().size();

        return new DefaultEntityConstraint(
                entity.getName(),
                allowsId,
                allowsAllAttributes,
                ap.attributes,
                ap.relationships);
    }

    class AccessibleProperties {
        Set<String> idParts = new HashSet<>();
        Set<String> attributes = new HashSet<>();
        Set<String> relationships = new HashSet<>();
    }
}
