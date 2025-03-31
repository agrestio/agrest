package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.AgException;

import java.io.IOException;
import java.util.List;

public class ListEncoder implements Encoder {

    private final Encoder elementEncoder;

    public ListEncoder(Encoder elementEncoder) {
        this.elementEncoder = elementEncoder;
    }

    @Override
    public void encode(String propertyName, Object object, boolean skipNullProperties, JsonGenerator out) throws IOException {

        if (propertyName != null) {
            out.writeFieldName(propertyName);
        }

        out.writeStartArray();

        for (Object o : toList(object)) {
            elementEncoder.encode(null, o, skipNullProperties, out);
        }

        out.writeEndArray();
    }

    private List<?> toList(Object object) {

        if (!(object instanceof List)) {

            if (object == null) {
                throw AgException.internalServerError("Unexpected null list");
            }

            throw AgException.internalServerError("Expected a List, got: %s", object.getClass().getName());
        }

        return (List<?>) object;
    }
}
