package io.agrest.constraints;

import io.agrest.meta.LrEntity;

import java.util.function.Function;

/**
 * Metadata constraint, essentially a function that creates {@link ConstrainedLrEntity} from {@link LrEntity}.
 *
 * @since 2.4
 */
public interface Constraint<T> extends Function<LrEntity<T>, ConstrainedLrEntity<T>> {

    /**
     * @param type a root type for constraints.
     * @param <T>  LinkRest entity type.
     * @return a new Constraints instance.
     */
    static <T> ConstraintsBuilder<T> excludeAll(Class<T> type) {
        return new ConstraintsBuilder<T>(Function.identity());
    }

    /**
     * @param type a root type for constraints.
     * @param <T>  LinkRest entity type.
     * @return a new Constraints instance.
     */
    static <T> ConstraintsBuilder<T> idOnly(Class<T> type) {
        return excludeAll(type).includeId();
    }

    /**
     * @param type a root type for constraints.
     * @param <T>  LinkRest entity type.
     * @return a new Constraints instance.
     */
    static <T> ConstraintsBuilder<T> idAndAttributes(Class<T> type) {
        return excludeAll(type).includeId().allAttributes();
    }
}
