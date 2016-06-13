package com.nhl.link.rest;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.encoder.EncoderVisitor;

/**
 * Encapsulates how certain data is extracted and encoded from entity objects.
 * Conceptually {@link EntityProperty} is not limited to object "bean"
 * properties. It can be some value calculated dynamically based on the request
 * context, such as current date, or some other parameters, etc.
 */
public interface EntityProperty {

    /**
     * Reads a property of a given object and encodes it to the provided JSON
     * output.
     */
    void encode(Object root, String propertyName, JsonGenerator out) throws IOException;

    /**
     * Returns a value of the named property of the object.
     *
     * @param root         an object whose property we are reading.
     * @param propertyName the name of the property to read.
     * @return property value or null if the object is null.
     * @since 2.0
     */
    Object read(Object root, String propertyName);

    /**
     * A graph traversal method that recursively visits all graph nodes that
     * will be encoded with this encoder.
     *
     * @since 2.0
     */
    int visit(Object object, String propertyName, EncoderVisitor visitor);
}
