package com.nhl.link.rest.runtime.encoder;

import com.nhl.link.rest.Entity;
import com.nhl.link.rest.EntityProperty;

/**
 * Provides an extension point for building custom attribute encoders if the
 * default encoders based on the ORM model are not good enough for any reason.
 */
public interface IAttributeEncoderFactory {

	EntityProperty getAttributeProperty(Entity<?> entity, String attributeName);

	EntityProperty getIdProperty(Entity<?> entity);
}
