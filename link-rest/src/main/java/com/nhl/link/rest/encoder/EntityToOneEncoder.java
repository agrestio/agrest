package com.nhl.link.rest.encoder;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

public class EntityToOneEncoder implements Encoder {

	private Encoder objectEncoder;

	public EntityToOneEncoder(Encoder objectEncoder) {
		this.objectEncoder = objectEncoder;
	}

	@Override
	public boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException {
		objectEncoder.encode(propertyName, object, out);
		return true;
	}

	@Override
	public boolean willEncode(String propertyName, Object object) {
		return true;
	}
}
