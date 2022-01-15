package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public abstract class AbstractEncoder implements Encoder {

	@Override
	public boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException {
		if (propertyName != null) {
			out.writeFieldName(propertyName);
		}

		if (object == null) {
			out.writeNull();
			return true;
		} else {
			return encodeNonNullObject(object, out);
		}
	}

	protected abstract boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException;
}
