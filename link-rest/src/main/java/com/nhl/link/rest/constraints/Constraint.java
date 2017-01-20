package com.nhl.link.rest.constraints;

import com.nhl.link.rest.meta.LrEntity;

import java.util.function.Function;

/**
 * Metadata constraint, essentially a function that creates {@link ConstrainedLrEntity} from {@link LrEntity}.
 *
 * @since 2.4
 */
public interface Constraint<T> extends Function<LrEntity<T>, ConstrainedLrEntity<T>> {
}
