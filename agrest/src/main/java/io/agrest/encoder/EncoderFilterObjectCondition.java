package io.agrest.encoder;

/**
 * @param <T>
 * @since 3.4
 */
@FunctionalInterface
public interface EncoderFilterObjectCondition<T> {

    boolean test(String propertyName, T object, Encoder delegate);
}
