package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

/**
 * @param <T>
 * @since 3.4
 */
@FunctionalInterface
public interface EncoderFilterEncoder<T> {

    boolean encode(String propertyName, T object, JsonGenerator out, Encoder delegate) throws IOException;
}
