package io.agrest.meta;

import io.agrest.ResourceEntity;
import io.agrest.property.PropertyReader;

/**
 * @since 1.12
 */
public interface AgRelationship {

    String getName();

    /**
     * @since 2.0
     */
    AgEntity<?> getTargetEntity();

    boolean isToMany();

    /**
     * Returns a {@link PropertyReader} for this relationship applicable in a context of the provided request entity.
     *
     * @since 3.4
     */
    PropertyReader getPropertyReader(ResourceEntity<?> entity);
}
