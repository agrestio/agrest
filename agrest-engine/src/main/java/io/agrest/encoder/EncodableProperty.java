package io.agrest.encoder;

import io.agrest.property.PropertyReader;

/**
 * Encapsulates how certain data is extracted and encoded from entity objects.
 *
 * @since 3.7
 */
public class EncodableProperty {

    private Encoder encoder;
    private PropertyReader reader;

    public static EncodableProperty property(PropertyReader reader) {
        return new EncodableProperty(reader, GenericEncoder.encoder());
    }

    private EncodableProperty(PropertyReader reader, Encoder encoder) {
        this.encoder = encoder;
        this.reader = reader;
    }

    public EncodableProperty encodedWith(Encoder encoder) {
        this.encoder = encoder;
        return this;
    }


    public Encoder getEncoder() {
        return encoder;
    }

    public PropertyReader getReader() {
        return reader;
    }

    public int visit(Object root, EncoderVisitor visitor) {
        Object value = root == null ? null : getReader().value(root);
        return encoder.visitEntities(value, visitor);
    }
}
