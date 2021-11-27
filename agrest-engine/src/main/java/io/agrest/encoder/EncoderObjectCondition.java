package io.agrest.encoder;

/**
 * @param <T>
 * @since 3.4
 * @deprecated since 4.8 in favor of {@link io.agrest.filter.ObjectFilter}.
 */
@Deprecated
@FunctionalInterface
public interface EncoderObjectCondition<T> {

    boolean test(String propertyName, T object, Encoder delegate);
}
