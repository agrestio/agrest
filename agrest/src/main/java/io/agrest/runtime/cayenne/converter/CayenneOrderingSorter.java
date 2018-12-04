package io.agrest.runtime.cayenne.converter;

import io.agrest.backend.util.converter.OrderingSorter;
import org.apache.cayenne.query.Ordering;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
public class CayenneOrderingSorter implements OrderingSorter<Ordering> {

    @Override
    public void accept(List<Ordering> orderings, List<?> objects) {
        if (!orderings.isEmpty() && objects.size() > 1) {

            // don't mess up underlying relationship, sort a copy...
            List list = new ArrayList<>(objects);

            Ordering.orderList(objects, orderings);
        }
    }
}
