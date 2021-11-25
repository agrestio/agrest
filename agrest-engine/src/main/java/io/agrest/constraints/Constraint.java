package io.agrest.constraints;

import io.agrest.meta.AgEntity;

import java.util.function.Function;

/**
 * Metadata constraint, essentially a function that creates {@link ConstrainedAgEntity} from {@link AgEntity}.
 *
 * @since 2.4
 * @deprecated since 4.8 in favor of non-hierarchical per-entity {@link io.agrest.access.PropertyAccessRules} API
 */
@Deprecated
public interface Constraint<T> extends Function<AgEntity<T>, ConstrainedAgEntity<T>> {

    /**
     * @param type a root type for constraints.
     * @param <T>  Agrest entity type.
     * @return a new Constraints instance.
     */
    static <T> ConstraintsBuilder<T> excludeAll(Class<T> type) {
        return new ConstraintsBuilder<T>(Function.identity());
    }

    /**
     * @param type a root type for constraints.
     * @param <T>  Agrest entity type.
     * @return a new Constraints instance.
     */
    static <T> ConstraintsBuilder<T> idOnly(Class<T> type) {
        return excludeAll(type).includeId();
    }

    /**
     * @param type a root type for constraints.
     * @param <T>  Agrest entity type.
     * @return a new Constraints instance.
     */
    static <T> ConstraintsBuilder<T> idAndAttributes(Class<T> type) {
        return excludeAll(type).includeId().allAttributes();
    }
}
