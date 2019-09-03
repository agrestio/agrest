package io.agrest.encoder;

/**
 * @param <T>
 * @since 3.4
 */
@FunctionalInterface
public interface EncoderObjectCondition<T> {

    boolean test(String propertyName, T object, Encoder delegate);
}
