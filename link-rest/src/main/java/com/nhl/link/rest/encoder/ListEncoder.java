package com.nhl.link.rest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.Ordering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListEncoder implements CollectionEncoder {

	private Encoder elementEncoder;
	private List<Ordering> orderings;
	private Expression filter;

	private int offset;
	private int limit;

	private boolean shouldFilter;

	public ListEncoder(Encoder elementEncoder) {
		this.elementEncoder = elementEncoder;
		this.orderings = Collections.emptyList();
	}

	public ListEncoder(Encoder elementEncoder, Expression filter, List<Ordering> orderings) {
		this.elementEncoder = elementEncoder;
		this.orderings = orderings;
		this.filter = filter;
	}

	@Override
	public int visitEntities(Object root, EncoderVisitor visitor) {
		List<?> objects = toList(root);

		Counter counter = new Counter();

		rewind(counter, objects, offset);
		return visit(counter, objects, limit > 0 ? limit : Integer.MAX_VALUE, visitor);
	}

	public ListEncoder withOffset(int offset) {
		this.offset = offset;
		return this;
	}

	public ListEncoder withLimit(int limit) {
		this.limit = limit;
		return this;
	}

	public ListEncoder shouldFilter() {
		shouldFilter = true;
		return this;
	}

	/**
	 * @since 2.1
     */
	public ListEncoder shouldFilter(boolean filter) {
		shouldFilter = filter;
		return this;
	}

	/**
	 * @since 2.0
	 */
	public int encodeAndGetTotal(String propertyName, Object object, JsonGenerator out) throws IOException {
		if (propertyName != null) {
			out.writeFieldName(propertyName);
		}

		List<?> objects = toList(object);

		out.writeStartArray();

		Counter counter = new Counter();

		// to get valid counts and offsets, we need to do the following:
		// rewind head -> encode -> rewind tail
		rewind(counter, objects, offset);
		encode(counter, objects, limit > 0 ? limit : Integer.MAX_VALUE, out);
		rewind(counter, objects, objects.size());

		out.writeEndArray();

		return counter.getTotal();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<?> toList(Object object) {

		if (object == null) {
			throw new IllegalStateException("Unexpected null list");
		}

		if (!(object instanceof List)) {
			throw new IllegalStateException(
					"Unexpected object type. Should be a List, got: " + object.getClass().getName());
		}

		List<?> list = (List) object;

		// sort list before encoding, but do not filter it - we can filter during encoding
		if (!orderings.isEmpty() && list.size() > 1) {

			// don't mess up underlying relationship, sort a copy...
			list = new ArrayList<>(list);

			Ordering.orderList(list, orderings);
		}

		return list;
	}

	private void rewind(Counter c, List<?> objects, int limit) {

		int length = objects.size();

		for (; c.position < length && c.rewound < limit; c.position++) {

			if (shouldFilter) {
				Object o = objects.get(c.position);
				if (filter == null || filter.match(o)) {
					if (elementEncoder.willEncode(null, o)) {
						c.rewound++;
					}
				}
			} else {
				c.rewound++;
			}
		}
	}

	private void encode(Counter c, List<?> objects, int limit, JsonGenerator out) throws IOException {

		int length = objects.size();

		for (; c.position < length && c.encoded < limit; c.position++) {

			Object o = objects.get(c.position);
			if (filter == null || filter.match(o)) {
				if (elementEncoder.encode(null, objects.get(c.position), out)) {
					c.encoded++;
				}
			}
		}
	}

	private int visit(Counter c, List<?> objects, int limit, EncoderVisitor visitor) {

		int length = objects.size();

		for (; c.position < length && c.encoded < limit; c.position++) {

			Object o = objects.get(c.position);
			if (filter == null || filter.match(o)) {

				int bitmask = elementEncoder.visitEntities(o, visitor);
				c.encoded++;

				if ((bitmask & VISIT_SKIP_ALL) != 0) {
					return VISIT_SKIP_ALL;
				}
			}
		}

		return VISIT_CONTINUE;
	}

	@Override
	public boolean willEncode(String propertyName, Object object) {
		return true;
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
