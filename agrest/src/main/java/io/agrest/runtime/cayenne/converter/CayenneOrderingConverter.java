package io.agrest.runtime.cayenne.converter;

import io.agrest.backend.util.converter.OrderingConverter;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;

/**
 *
 *
 */
public class CayenneOrderingConverter implements OrderingConverter<Ordering> {

    @Override
    public Ordering apply(io.agrest.backend.query.Ordering from) {
        return new Ordering(from.getSortSpecString(), SortOrder.valueOf(from.getSortOrder().name()));
    }
}
