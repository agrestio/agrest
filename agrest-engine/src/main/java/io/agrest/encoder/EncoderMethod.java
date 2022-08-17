package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.access.ReadFilter;

import java.io.IOException;

/**
 * @param <T>
 * @since 3.4
 * @deprecated since 4.8 in favor of {@link ReadFilter}.
 */
@Deprecated
@FunctionalInterface
public interface EncoderMethod<T> {

    boolean encode(String propertyName, T object, JsonGenerator out, Encoder delegate) throws IOException;
}