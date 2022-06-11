package io.agrest.encoder;

import io.agrest.reader.DataReader;

/**
 * Encapsulates how certain data is extracted and encoded from entity objects.
 *
 * @since 3.7
 */
public class EncodableProperty {

    private Encoder encoder;
    private final DataReader reader;

    public static EncodableProperty property(DataReader reader) {
        return new EncodableProperty(reader, GenericEncoder.encoder());
    }

    private EncodableProperty(DataReader reader, Encoder encoder) {
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

    public DataReader getReader() {
        return reader;
    }
}
