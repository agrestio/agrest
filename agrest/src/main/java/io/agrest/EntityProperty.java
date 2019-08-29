package io.agrest;

import io.agrest.encoder.Encoder;
import io.agrest.encoder.EncoderVisitor;
import io.agrest.property.PropertyReader;

/**
 * Encapsulates how certain data is extracted and encoded from entity objects.
 * Conceptually {@link EntityProperty} is not limited to object "bean"
 * properties. It can be some value calculated dynamically based on the request
 * context, such as current date, or some other parameters, etc.
 */
public interface EntityProperty {

    /**
     * @since 3.4
     */
    Encoder getEncoder();

    /**
     * @since 3.4
     */
    PropertyReader getReader();

    /**
     * A graph traversal method that recursively visits all graph nodes that
     * will be encoded with this encoder.
     *
     * @since 2.0
     */
    int visit(Object object, String propertyName, EncoderVisitor visitor);
}
