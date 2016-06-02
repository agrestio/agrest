package com.nhl.link.rest.client.runtime.run;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.cayenne.exp.Expression;

import com.nhl.link.rest.client.LinkRestClientException;
import com.nhl.link.rest.client.protocol.Include;
import com.nhl.link.rest.client.protocol.Sort;

/**
 * @since 2.0
 */
class RequestEncoder {

	private static final RequestEncoder instance = new RequestEncoder();

	public static RequestEncoder encoder() {
		return instance;
	}

	public String encode(Include include) {
		return urlEncode(asString(include));
	}

	private String asString(Include include) {
		if (include.isSimple()) {
			return include.getPath();
		}

		StringBuilder buf = new StringBuilder();
		buf.append("{\"path\":\"");
		buf.append(include.getPath());
		buf.append("\"");

		include.getMapBy().ifPresent(mapBy -> {
			buf.append(",\"mapBy\":\"");
			buf.append(mapBy);
			buf.append("\"");
		});

		include.getCayenneExp().ifPresent(exp -> {
			buf.append(",\"cayenneExp\":\"");
			buf.append(encode(exp, false));
			buf.append("\"");
		});

		include.getStart().ifPresent(start -> {
			buf.append(",\"start\":");
			buf.append(start);
		});

		include.getLimit().ifPresent(limit -> {
			buf.append(",\"limit\":");
			buf.append(limit);
		});

		if (!include.getOrderings().isEmpty()) {
			buf.append(",\"sort\":");
			buf.append(encode(include.getOrderings(), false));
		}

		buf.append("}");
		return buf.toString();
	}

	public String encode(Expression expression) {
		return encode(expression, true);
	}

	private String encode(Expression expression, boolean shouldEncodeUrl) {

		if (shouldEncodeUrl) {
			return urlEncode(expression.toString());
		} else {
			return expression.toString().replace('"', '\'');
		}
	}

	public String encode(Collection<Sort> orderings) {
		return encode(orderings, true);
	}

	private String encode(Collection<Sort> orderings, boolean shouldEncodeUrl) {

		if (orderings == null || orderings.isEmpty()) {
			return null;
		}

		String result;

		if (orderings.size() == 1) {
			result = encodeSorts(Collections.singleton(orderings.iterator().next()));
		} else {
			result = encodeSorts(orderings);
		}

		return shouldEncodeUrl ? urlEncode(result) : result;
	}

	private String encodeSorts(Collection<Sort> orderings) {

		if (orderings == null || orderings.isEmpty()) {
			return null;
		}

		StringBuilder buf = new StringBuilder();
		buf.append("[");

		Iterator<Sort> iter = orderings.iterator();
		while (iter.hasNext()) {
			buf.append(encodeSort(iter.next()));
			if (iter.hasNext()) {
				buf.append(",");
			}
		}

		buf.append("]");
		return buf.toString();
	}

	private String encodeSort(Sort ordering) {

		if (ordering == null) {
			return null;
		}

		StringBuilder buf = new StringBuilder();
		buf.append("{\"property\":\"");
		buf.append(ordering.getPropertyName());
		buf.append("\"");

		if (ordering.getDirection() != Sort.SortDirection.ASCENDING) {
			buf.append(",\"direction\":\"");
			buf.append(ordering.getDirection().abbrev());
			buf.append("\"");
		}
		;

		buf.append("}");
		return buf.toString();
	}

	private String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new LinkRestClientException("Unexpected error", e);
		}
	}
}
