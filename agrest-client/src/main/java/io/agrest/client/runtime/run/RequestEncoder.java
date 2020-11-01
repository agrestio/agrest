package io.agrest.client.runtime.run;

import io.agrest.client.AgClientException;
import io.agrest.client.protocol.Expression;
import io.agrest.client.protocol.Include;
import io.agrest.client.protocol.Sort;
import io.agrest.base.protocol.Dir;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

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

		include.getExp().ifPresent(exp -> {
			buf.append(",\"exp\":");
			buf.append(encode(exp, false));
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

		String query = expression.getQuery().replace('"', '\'');

		String result;
		switch (expression.getParamsType()) {

			case NO_PARAMS: {
				result = query;
				break;
			}
			case POSITIONAL: {

				StringBuilder buf = new StringBuilder();
				buf.append("[\"");
				buf.append(query);
				buf.append("\"");

				for (Object param : expression.getParams()) {
					buf.append(",\"");
					buf.append(param);
					buf.append("\"");
				}

				buf.append("]");
				result = buf.toString();
				break;
			}
			case NAMED: {

				StringBuilder buf = new StringBuilder();
				buf.append("{\"exp\":\"");
				buf.append(query);
				buf.append("\",\"params\":{");

				Iterator<Map.Entry<String, Object>> iter = expression.getParamsMap().entrySet().iterator();
				while (iter.hasNext()) {

					Map.Entry<String, Object> param = iter.next();
					buf.append("\"");
					buf.append(param.getKey());
					buf.append("\":\"");
					buf.append(param.getValue());
					buf.append("\"");

					if (iter.hasNext()) {
						buf.append(",");
					}
				}

				buf.append("}}");
				result = buf.toString();
				break;
			}
			default: {
				throw new AgClientException("Unexpected expression params type: " + expression.getParamsType().name());
			}
		}

		return shouldEncodeUrl? urlEncode(result) : result;
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

		if (ordering.getDirection() != Dir.ASC) {
			buf.append(",\"direction\":\"");
			buf.append(ordering.getDirection().name());
			buf.append("\"");
		}

		buf.append("}");
		return buf.toString();
	}

	private String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new AgClientException("Unexpected error", e);
		}
	}
}
