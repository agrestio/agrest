package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.converter.valuestring.ValueStringConverter;

import java.io.IOException;

/**
 * A generic value encoder based on a {@link ValueStringConverter}.
 *
 * @since 5.0
 */
public class ValueEncoder extends AbstractEncoder {

    private final ValueStringConverter converter;

    public ValueEncoder(ValueStringConverter converter) {
        this.converter = converter;
    }

    @Override
    protected void encodeNonNullObject(Object object, boolean skipNullProperties, JsonGenerator out) throws IOException {
        out.writeObject(converter.asString(object));
    }
}
