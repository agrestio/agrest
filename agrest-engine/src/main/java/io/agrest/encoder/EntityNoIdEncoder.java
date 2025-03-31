package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Map;

/**
 * @since 3.4
 */
public class EntityNoIdEncoder extends AbstractEncoder {

    private final Map<String, EncodableProperty> encoders;

    public EntityNoIdEncoder(Map<String, EncodableProperty> encoders) {
        this.encoders = encoders;
    }

    @Override
    protected void encodeNonNullObject(Object object, boolean skipNullProperties, JsonGenerator out) throws IOException {
        out.writeStartObject();
        encodeProperties(object, skipNullProperties, out);
        out.writeEndObject();
    }

    protected void encodeProperties(Object object, boolean skipNullProperties, JsonGenerator out) throws IOException {

        for (Map.Entry<String, EncodableProperty> e : encoders.entrySet()) {
            EncodableProperty p = e.getValue();
            String propertyName = e.getKey();
            Object v = object == null ? null : p.getReader().read(object);
            p.getEncoder().encode(propertyName, v, skipNullProperties, out);
        }
    }
}
