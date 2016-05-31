package com.nhl.link.rest.client;

import org.apache.cayenne.exp.Expression;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
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
                buf.append(encode(include.getCayenneExp()));
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

        try {
            return URLEncoder.encode(result, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new LinkRestClientException("Unexpected error", e);
        }
    }

    public String encode(Expression expression) {
        // TODO: encode expression with params (if there are any)
        return null;
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
            result = encodeSort(orderings.iterator().next());
        } else {
            result = encodeSorts(orderings);
        }

        if (shouldEncodeUrl) {
            try {
                return URLEncoder.encode(result, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new LinkRestClientException("Unexpected error", e);
            }
        } else {
            return result;
        }
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
}
