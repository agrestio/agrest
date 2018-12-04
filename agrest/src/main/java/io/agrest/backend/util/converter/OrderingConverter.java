package io.agrest.backend.util.converter;

import io.agrest.backend.query.Ordering;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 *
 */
public interface OrderingConverter<R> extends Function<Ordering, R> {


    default List<R> apply(List<Ordering> orderings) {
        return orderings.stream().map(o -> apply(o)).collect(Collectors.toList());
    }
}
