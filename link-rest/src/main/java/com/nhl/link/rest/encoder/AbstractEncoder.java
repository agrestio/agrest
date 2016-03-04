package com.nhl.link.rest.encoder;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

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

	/**
	 * Always returns true.
	 */
	@Override
	public boolean willEncode(String propertyName, Object object) {
		return true;
	}

	protected abstract boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException;
}
