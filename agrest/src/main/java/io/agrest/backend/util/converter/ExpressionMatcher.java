package io.agrest.backend.util.converter;


import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 *
 */
public interface ExpressionMatcher<T> extends BiFunction<T, Object, Boolean> {

    default Function<Object, Boolean> match (T t) {

        return o -> apply(t, o);
    }

}
