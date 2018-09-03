package io.agrest.runtime.encoder;

import io.agrest.EntityProperty;
import io.agrest.ResourceEntity;
import io.agrest.encoder.Encoder;
import io.agrest.meta.LrAttribute;
import io.agrest.meta.LrEntity;
import io.agrest.meta.LrRelationship;

/**
 * Provides an extension point for building custom attribute encoders if the
 * default encoders based on the ORM model are not good enough for any reason.
 */
public interface IAttributeEncoderFactory {

	/**
	 * @since 1.23
	 */
	EntityProperty getAttributeProperty(LrEntity<?> entity, LrAttribute attribute);

	/**
	 * @since 1.23
	 */
	EntityProperty getRelationshipProperty(LrEntity<?> entity, LrRelationship relationship, Encoder encoder);

	EntityProperty getIdProperty(ResourceEntity<?> entity);
}
