package io.agrest.encoder;

import io.agrest.access.ReadFilter;

/**
 * @param <T>
 * @since 3.4
 * @deprecated since 4.8 in favor of {@link ReadFilter}.
 */
@Deprecated
@FunctionalInterface
public interface EncoderObjectCondition<T> {

    boolean test(String propertyName, T object, Encoder delegate);
}
