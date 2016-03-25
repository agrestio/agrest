package com.nhl.link.rest.runtime.encoder;

import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;

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
