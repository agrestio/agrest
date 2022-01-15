package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

/**
 * An object for encoding JSON field/value pairs using Jackson API.
 */
public interface Encoder {

    /**
     * Encodes provided object into {@link JsonGenerator}. Encoder should encode "propertyName" (if not null) and a
     * matching object.
     *
     * @param propertyName Specifies the "incoming" property that points to the current object from its parent object.
     *                     This argument can be null, in which case we are dealing with a root object, and property
     *                     name should not be encoded.
     * @param object       object to encode
     * @param out          output object where encoded JSON should be written.
     */
    void encode(String propertyName, Object object, JsonGenerator out) throws IOException;
}
