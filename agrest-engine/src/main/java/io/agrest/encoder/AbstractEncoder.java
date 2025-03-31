package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public abstract class AbstractEncoder implements Encoder {

    @Override
    public void encode(String propertyName, Object object, boolean skipNullProperties, JsonGenerator out) throws IOException {

        if (object == null) {
            if (!skipNullProperties) {
                encodePropertyName(propertyName, out);
                out.writeNull();
            }
        } else {
            encodePropertyName(propertyName, out);
            encodeNonNullObject(object, skipNullProperties, out);
        }
    }

    protected void encodePropertyName(String propertyName, JsonGenerator out) throws IOException {
        if (propertyName != null) {
            out.writeFieldName(propertyName);
        }
    }

    protected abstract void encodeNonNullObject(Object object, boolean skipNullProperties, JsonGenerator out) throws IOException;
}
