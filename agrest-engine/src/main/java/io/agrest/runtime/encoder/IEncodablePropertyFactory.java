package io.agrest.runtime.encoder;

import io.agrest.ResourceEntity;
import io.agrest.encoder.EncodableProperty;
import io.agrest.encoder.Encoder;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.ProcessingContext;

import java.util.Optional;

/**
 * Creates {@link EncodableProperty} objects for ResourceEntity attributes and relationships.
 *
 * @since 3.7
 */
public interface IEncodablePropertyFactory {

    /**
     * @since 3.4
     */
    EncodableProperty getAttributeProperty(ResourceEntity<?> entity, AgAttribute attribute);

    /**
     * @since 1.23
     */
    EncodableProperty getRelationshipProperty(
            ResourceEntity<?> entity,
            AgRelationship relationship,
            Encoder relatedEncoder,
            ProcessingContext<?> context);

    /**
     * Returns a property to read and encode entity ID. The id may have one more more attributes (all hidden bihind
     * {@link EncodableProperty} facade. If the entity has no ID defined, an empty Optional is returned.
     *
     * @since 3.4
     */
    Optional<EncodableProperty> getIdProperty(ResourceEntity<?> entity);
}
