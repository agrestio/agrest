package io.agrest.runtime.cayenne.converter;

import io.agrest.backend.util.converter.Converter;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;

/**
 *
 *
 */
public class CayenneOrderingConverter implements Converter<io.agrest.backend.query.Ordering, Ordering> {

    @Override
    public Ordering convert(io.agrest.backend.query.Ordering from) {
        return new Ordering(from.getSortSpecString(), SortOrder.valueOf(from.getSortOrder().name()));
    }
}
