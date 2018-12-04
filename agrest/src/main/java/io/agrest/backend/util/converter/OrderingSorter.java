package io.agrest.backend.util.converter;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 *
 *
 */
public interface OrderingSorter<T> extends BiConsumer<List<T>, List<?>> {

    default Consumer<List<?>> sort(List<T> orderings) {

        return t -> accept(orderings, t);
    }
}
