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

public class ListEncoder extends AbstractEncoder {

	private Encoder elementEncoder;
	private Collection<Ordering> orderings;
	private Expression filter;

	public ListEncoder(Encoder elementEncoder, Expression filter, Collection<Ordering> orderings) {
		this.elementEncoder = elementEncoder;
		this.orderings = orderings;
		this.filter = filter;
	}

	@Override
	protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {

		List<?> list = toList(object);

		out.writeStartArray();

		if (filter != null) {
			encodeWithFilter(list, out);
		} else {
			encodeWithNoFilter(list, out);
		}

		out.writeEndArray();
		return true;
	}

	private void encodeWithFilter(List<?> list, JsonGenerator out) throws IOException {
		for (Object o : list) {
			if (filter.match(o)) {
				elementEncoder.encode(null, o, out);
			}
		}
	}

	private void encodeWithNoFilter(List<?> list, JsonGenerator out) throws IOException {
		for (Object o : list) {
			elementEncoder.encode(null, o, out);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<?> toList(Object object) {

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

}
