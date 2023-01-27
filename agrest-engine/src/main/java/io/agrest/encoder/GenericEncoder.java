package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public class GenericEncoder extends AbstractEncoder {

	private static final Encoder instance = new GenericEncoder();

	public static Encoder encoder() {
		return instance;
	}

	private GenericEncoder() {
	}

	@Override
	protected void encodeNonNullObject(Object object, boolean skipNullProperties, JsonGenerator out) throws IOException {
		out.writeObject(object);
	}
}
