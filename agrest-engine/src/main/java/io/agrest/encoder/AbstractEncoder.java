package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public abstract class AbstractEncoder implements Encoder {

    @Override
    public void encode(String propertyName, Object object, JsonGenerator out) throws IOException {
        if (propertyName != null) {
            out.writeFieldName(propertyName);
        }

        if (object == null) {
            out.writeNull();
        } else {
            encodeNonNullObject(object, out);
        }
    }

    protected abstract void encodeNonNullObject(Object object, JsonGenerator out) throws IOException;
}
