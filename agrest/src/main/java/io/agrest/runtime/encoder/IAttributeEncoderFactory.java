package io.agrest.runtime.encoder;

import io.agrest.EntityProperty;
import io.agrest.ResourceEntity;
import io.agrest.encoder.Encoder;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgRelationship;

/**
 * Provides an extension point for building custom attribute encoders if the
 * default encoders based on the ORM model are not good enough for any reason.
 */
public interface IAttributeEncoderFactory {

	/**
	 * @since 1.23
	 */
	EntityProperty getAttributeProperty(ResourceEntity<?> entity, AgAttribute attribute);

	/**
	 * @since 1.23
	 */
	EntityProperty getRelationshipProperty(ResourceEntity<?> entity, AgRelationship relationship, Encoder encoder);

	EntityProperty getIdProperty(ResourceEntity<?> entity);
}
