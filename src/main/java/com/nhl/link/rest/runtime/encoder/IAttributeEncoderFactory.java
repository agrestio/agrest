package com.nhl.link.rest.runtime.encoder;

import com.nhl.link.rest.ClientEntity;
import com.nhl.link.rest.ClientProperty;

/**
 * Provides an extension point for building custom attribute encoders if the
 * default encoders based on the ORM model are not good enough for any reason.
 */
public interface IAttributeEncoderFactory {

	ClientProperty getAttributeProperty(ClientEntity<?> entity, String attributeName);

	ClientProperty getIdProperty(ClientEntity<?> entity);
}
