package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

/**
 * @param <T>
 * @since 3.4
 * @deprecated since 4.8 in favor of {@link io.agrest.filter.ObjectFilter}.
 */
@Deprecated
@FunctionalInterface
public interface EncoderMethod<T> {

    boolean encode(String propertyName, T object, JsonGenerator out, Encoder delegate) throws IOException;
}
