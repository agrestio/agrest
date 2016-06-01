package com.nhl.link.rest.client;

import org.apache.cayenne.exp.Expression;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class ConstraintEncoder {

    private static final ConstraintEncoder instance = new ConstraintEncoder();

    public static ConstraintEncoder encoder() {
        return instance;
    }

    public String encode(Include include) {

        String result;

        if (include.isConstrained()) {
            StringBuilder buf = new StringBuilder();
            buf.append("{\"path\":\"");
            buf.append(include.getPath());
            buf.append("\"");

            if (include.getMapBy() != null) {
                buf.append(",\"mapBy\":\"");
                buf.append(include.getMapBy());
                buf.append("\"");
            }

            if (include.getCayenneExp() != null) {
                buf.append(",\"cayenneExp\":\"");
                buf.append(encode(include.getCayenneExp(), false));
                buf.append("\"");
            }

            if (include.getStart() != null) {
                buf.append(",\"start\":");
                buf.append(include.getStart());
            }

            if (include.getLimit() != null) {
                buf.append(",\"limit\":");
                buf.append(include.getLimit());
            }

            if (!include.getOrderings().isEmpty()) {
                buf.append(",\"sort\":");
                buf.append(encode(include.getOrderings(), false));
            }

            buf.append("}");
            result = buf.toString();

        } else {
            result = include.getPath();
        }

        return urlEncode(result);
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

        return shouldEncodeUrl? urlEncode(result) : result;
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
        };

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
