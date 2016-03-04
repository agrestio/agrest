package com.nhl.link.rest.encoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.Ordering;
import org.apache.commons.collections.ComparatorUtils;

import com.fasterxml.jackson.core.JsonGenerator;

public class ListEncoder implements Encoder {

	private Encoder elementEncoder;
	private Collection<Ordering> orderings;
	private Expression filter;

	private String totalKey;
	private int offset;
	private int limit;

	public ListEncoder(Encoder elementEncoder) {
		this.elementEncoder = elementEncoder;
		this.orderings = Collections.emptyList();
	}

	public ListEncoder(Encoder elementEncoder, Expression filter, Collection<Ordering> orderings) {
		this.elementEncoder = elementEncoder;
		this.orderings = orderings;
		this.filter = filter;
	}

	public ListEncoder withTotal(String totalKey) {
		this.totalKey = totalKey;
		return this;
	}

	public ListEncoder withOffset(int offset) {
		this.offset = offset;
		return this;
	}

	public ListEncoder withLimit(int limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException {

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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<?> toList(Object object) {

		if (object == null) {
			throw new IllegalStateException("Unexpected null list");
		}

		if (!(object instanceof List)) {
			throw new IllegalStateException("Unexpected object type. Should be a List, got: "
					+ object.getClass().getName());
		}

		List<?> list = (List) object;

		// sort list before encoding, but do not filter it - we can filter
		// during encoding
		if (!orderings.isEmpty() && list.size() > 1) {

			// don't mess up underlying relationship, sort a copy...
			list = new ArrayList(list);

			Collections.sort(list, ComparatorUtils.chainedComparator(orderings));
		}

		return list;
	}

	private void rewind(Counter c, List<?> objects, int limit) throws IOException {

		int length = objects.size();

		for (; c.position < length && c.rewound < limit; c.position++) {

			Object o = objects.get(c.position);
			if (filter == null || filter.match(o)) {
				if (elementEncoder.willEncode(null, o)) {
					c.rewound++;
				}
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
