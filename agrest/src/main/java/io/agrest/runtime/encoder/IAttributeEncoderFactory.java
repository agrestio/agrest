package io.agrest.runtime.encoder;

import io.agrest.EntityProperty;
import io.agrest.ResourceEntity;
import io.agrest.encoder.Encoder;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;

/**
 * A service that manages {@link EntityProperty} objects that allow to extract and encode entity attributes when rendering
 * JSON.
 */
public interface IAttributeEncoderFactory {

    /**
     * @since 1.23
     */
    EntityProperty getAttributeProperty(AgEntity<?> entity, AgAttribute attribute);

    /**
     * @since 1.23
     */
    EntityProperty getRelationshipProperty(ResourceEntity<?> entity, AgRelationship relationship, Encoder encoder);

    EntityProperty getIdProperty(ResourceEntity<?> entity);
}
