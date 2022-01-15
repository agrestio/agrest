package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

/**
 * @since 2.1
 */
public interface CollectionEncoder extends Encoder {

    int encodeAndGetTotal(String propertyName, Object object, JsonGenerator out) throws IOException;

    @Override
    default void encode(String propertyName, Object object, JsonGenerator out) throws IOException {
        encodeAndGetTotal(propertyName, object, out);
    }
}