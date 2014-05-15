package com.nhl.link.rest.encoder;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * @since 6.9
 */
public class RootListEncoder implements Encoder {

	private Encoder elementEncoder;

	private String totalKey;
	private int offset;
	private int limit;

	public RootListEncoder(Encoder elementEncoder) {
		this.elementEncoder = elementEncoder;
	}

	public RootListEncoder withTotal(String totalKey) {
		this.totalKey = totalKey;
		return this;
	}

	public RootListEncoder withOffset(int offset) {
		this.offset = offset;
		return this;
	}

	public RootListEncoder withLimit(int limit) {
		this.limit = limit;
		return this;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException {
		if (propertyName != null) {
			out.writeFieldName(propertyName);
		}

		if (object == null) {
			throw new IllegalStateException("Unexpected null list");
		}

		if (!(object instanceof List)) {
			throw new IllegalStateException("Unexpected object type. Should be a List, got: "
					+ object.getClass().getName());
		}

		out.writeStartArray();

		List<?> objects = (List) object;
		Counter counter = new Counter();

		// to get valid counts and offsets, we need to do the following:
		// rewind head -> encode -> rewind tail
		rewind(counter, objects, offset);
		encode(counter, objects, limit > 0 ? limit : Integer.MAX_VALUE, out);
		rewind(counter, objects, objects.size());

		out.writeEndArray();

		// checking for 'propertyName', not just 'totalKey', as if we skipped
		// encoding the field name above, we are not in the right place to
		// encode the totals.
		if (propertyName != null && totalKey != null) {
			out.writeFieldName(totalKey);
			out.writeNumber(counter.getTotal());
		}

		// regardless of the list contents, our encoding has succeeded...
		return true;
	}

	@Override
	public boolean willEncode(String propertyName, Object object) {
		return true;
	}

	private void rewind(Counter c, List<?> objects, int limit) throws IOException {

		int length = objects.size();

		for (; c.position < length && c.rewound < limit; c.position++) {
			if (elementEncoder.willEncode(null, objects.get(c.position))) {
				c.rewound++;
			}
		}
	}

	private void encode(Counter c, List<?> objects, int limit, JsonGenerator out) throws IOException {

		int length = objects.size();

		for (; c.position < length && c.encoded < limit; c.position++) {
			if (elementEncoder.encode(null, objects.get(c.position), out)) {
				c.encoded++;
			}
		}
	}

	final class Counter {
		int position;

		int encoded;
		int rewound;

		int getTotal() {
			return encoded + rewound;
		}
	}
}
