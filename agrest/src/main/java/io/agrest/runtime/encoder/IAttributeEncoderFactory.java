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

    /**
     * Returns a property to read and encode entity ID. The id may have one more more attributes (all hidden bihind
     * {@link EntityProperty} facade. If the entity has no ID defined, an empty Optional is returned.
     *
     * @since 3.4
     */
    Optional<EntityProperty> getIdProperty(ResourceEntity<?> entity);
}
