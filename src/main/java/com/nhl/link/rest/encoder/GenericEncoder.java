package com.nhl.link.rest.encoder;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

public class GenericEncoder extends AbstractEncoder {

	private static final Encoder instance = new GenericEncoder();

	public static Encoder encoder() {
		return instance;
	}

	private GenericEncoder() {
	}

	@Override
	protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
		out.writeObject(object);
		return true;
	}

}
