package io.agrest.filter;

import io.agrest.base.protocol.Exp;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Per-entity filter of objects intended to implement data access rules. Some filters can also be resolved to {@link Exp}
 * objects, allowing backends to apply them to the data query, optimizing filtering.
 *
 * @since 4.8
 */
@FunctionalInterface
public interface ObjectFilter {

    static <T> ObjectFilter predicate(Predicate<T> predicate) {
        return o -> predicate.test((T) o);
    }

    static ObjectFilter exp(String template) {
        return new ExpObjectFilter(Exp.simple(template));
    }

    static ObjectFilter expWithPositionalParams(String template, Object... params) {
        return new ExpObjectFilter(Exp.withPositionalParams(template, params));
    }

    static ObjectFilter expWithNamedParams(String template, Map<String, Object> params) {
        return new ExpObjectFilter(Exp.withNamedParams(template, params));
    }

    boolean isAccessible(Object object);

    /**
     * Returns an empty Optional. Some filters can be represented as {@link Exp} instances. Those should implement this
     * method to return an Exp equivalent of the filtering logic. For those Agrest backend may decide to evaluate
     * expression as a part of data query instead of applying the filter in memory.
     */
    default Optional<Exp> asExp() {
        return Optional.empty();
    }
}
