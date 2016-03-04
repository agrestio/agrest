package com.nhl.link.rest.runtime.encoder;

import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;

/**
 * Provides an extension point for building custom attribute encoders if the
 * default encoders based on the ORM model are not good enough for any reason.
 */
public interface IAttributeEncoderFactory {

	/**
	 * @since 1.12
	 */
	EntityProperty getAttributeProperty(ResourceEntity<?> entity, LrAttribute attribute);

	EntityProperty getIdProperty(ResourceEntity<?> entity);
}
