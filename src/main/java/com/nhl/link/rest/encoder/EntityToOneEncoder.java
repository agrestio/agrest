package com.nhl.link.rest.encoder;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.ClientProperty;

public abstract class EntityToOneEncoder implements Encoder {

	private ClientProperty idEncoder;
	private Encoder objectEncoder;

	public EntityToOneEncoder(Encoder objectEncoder, ClientProperty idEncoder) {
		this.idEncoder = idEncoder;
		this.objectEncoder = objectEncoder;
	}

	@Override
	public boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException {

		objectEncoder.encode(propertyName, object, out);

		// encode FK as 'xyz_id' property
		if (propertyName != null) {
			String idPropertyName = idPropertyName(propertyName);
			idEncoder.encode(object, idPropertyName, out);
		}
		
		return true;
	}

	@Override
	public boolean willEncode(String propertyName, Object object) {
		return true;
	}

	protected abstract String idPropertyName(String propertyName);
}
