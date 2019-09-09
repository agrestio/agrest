package io.agrest.runtime.encoder;

import io.agrest.EntityProperty;
import io.agrest.ResourceEntity;
import io.agrest.encoder.Encoder;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgRelationship;

import java.util.Optional;

/**
 * A service that manages {@link EntityProperty} objects that allow to extract and encode entity attributes when rendering
 * JSON.
 */
public interface IAttributeEncoderFactory {

    /**
     * @since 3.4
     */
    EntityProperty getAttributeProperty(ResourceEntity<?> entity, AgAttribute attribute);

    /**
     * @since 1.23
     */
    EntityProperty getRelationshipProperty(ResourceEntity<?> entity, AgRelationship relationship, Encoder encoder);

    Optional<EntityProperty> getIdProperty(ResourceEntity<?> entity);
}
