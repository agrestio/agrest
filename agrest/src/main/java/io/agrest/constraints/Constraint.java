package io.agrest.constraints;

import io.agrest.meta.AgEntity;

import java.util.function.Function;

/**
 * Metadata constraint, essentially a function that creates {@link ConstrainedAgEntity} from {@link AgEntity}.
 *
 * @since 2.4
 */
public interface Constraint<T, E> extends Function<AgEntity<T>, ConstrainedAgEntity<T, E>> {

    /**
     * @param type a root type for constraints.
     * @param <T>  Agrest entity type.
     * @return a new Constraints instance.
     */
    static <T, E> ConstraintsBuilder<T, E> excludeAll(Class<T> type) {
        return new ConstraintsBuilder<T, E>(Function.identity());
    }

    /**
     * @param type a root type for constraints.
     * @param <T>  Agrest entity type.
     * @param <E>  Agrest expression type.
     * @return a new Constraints instance.
     */
    static <T, E> ConstraintsBuilder<T, E> excludeAll(Class<T> type, Class<E> exp) {
        return new ConstraintsBuilder<T, E>(Function.identity());
    }


    /**
     * @param type a root type for constraints.
     * @param <T>  Agrest entity type.
     * @return a new Constraints instance.
     */
    static <T, E> ConstraintsBuilder<T, E> idOnly(Class<T> type) {
        return (ConstraintsBuilder<T, E>)excludeAll(type).includeId();
    }

    /**
     * @param type a root type for constraints.
     * @param <T>  Agrest entity type.
     * @return a new Constraints instance.
     */
    static <T, E> ConstraintsBuilder<T, E> idAndAttributes(Class<T> type) {
        return (ConstraintsBuilder<T, E>)excludeAll(type).includeId().allAttributes();
    }
}
