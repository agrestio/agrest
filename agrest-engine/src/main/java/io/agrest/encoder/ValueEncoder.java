package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.converter.valuejson.ValueJsonConverter;

import java.io.IOException;

/**
 * A generic value encoder based on a {@link ValueJsonConverter}.
 *
 * @since 5.0
 */
public class ValueEncoder extends AbstractEncoder {

    private final ValueJsonConverter converter;

    public ValueEncoder(ValueJsonConverter converter) {
        this.converter = converter;
    }

    @Override
    protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
        out.writeObject(converter.asString(object));
        return true;
    }
}
