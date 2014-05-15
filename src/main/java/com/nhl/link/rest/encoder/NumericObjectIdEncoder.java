package com.nhl.link.rest.encoder;

import java.io.IOException;
import java.util.Map;

import org.apache.cayenne.ObjectId;

import com.fasterxml.jackson.core.JsonGenerator;

public class NumericObjectIdEncoder extends AbstractEncoder {

	private static final Encoder instance = new NumericObjectIdEncoder();

	public static Encoder encoder() {
		return instance;
	}

	private NumericObjectIdEncoder() {
	}

	@Override
	protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {

		ObjectId id = (ObjectId) object;

		if (id.isTemporary()) {
			throw new IllegalArgumentException("Can't serialize temporary ObjectId: " + id);
		}

		Map<String, Object> values = id.getIdSnapshot();

		if (values.size() != 1) {
			throw new IllegalArgumentException("Can't serialize multi-value ObjectId: " + id);
		}

		Object value = values.entrySet().iterator().next().getValue();
		if (!(value instanceof Number)) {
			throw new IllegalArgumentException("PK is not a number: " + id);
		}

		out.writeNumber(((Number) value).longValue());
		return true;
	}
}
