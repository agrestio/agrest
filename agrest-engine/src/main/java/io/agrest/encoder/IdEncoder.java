package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.AgException;

import java.io.IOException;
import java.util.Map;

/**
 * @since 1.2
 */
public class IdEncoder implements Encoder {

    private Encoder valueEncoder;
    private Map<String, Encoder> valueEncoders;
    private boolean isCompoundId;

    public IdEncoder(Encoder valueEncoder) {
        this.valueEncoder = valueEncoder;
    }

    public IdEncoder(Map<String, Encoder> valueEncoders) {
        this.valueEncoders = valueEncoders;
        this.isCompoundId = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void encode(String propertyName, Object object, JsonGenerator out) throws IOException {
        if (object == null) {

            if (propertyName != null) {
                out.writeFieldName(propertyName);
            }
            out.writeNull();
        } else {
            encodeId(propertyName, (Map<String, Object>) object, out);
        }
    }

    protected void encodeId(String propertyName, Map<String, Object> id, JsonGenerator out) throws IOException {
        if (isCompoundId) {
            encodeCompoundId(propertyName, id, valueEncoders, out);
        } else {
            encodeSingleId(propertyName, id, valueEncoder, out);
        }
    }

    private void encodeSingleId(String propertyName, Map<String, Object> values,
                                Encoder valueEncoder, JsonGenerator out) throws IOException {

        if (values.size() != 1) {
            throw new IllegalArgumentException("Can't serialize multi-value ObjectId: " + values);
        }

        Object value = values.entrySet().iterator().next().getValue();
        valueEncoder.encode(propertyName, value, out);
    }

    private void encodeCompoundId(String propertyName, Map<String, Object> values,
                                  Map<String, Encoder> valueEncoders, JsonGenerator out) throws IOException {

        if (propertyName != null) {
            out.writeFieldName(propertyName);
        }

        out.writeStartObject();

        for (Map.Entry<String, Encoder> entry : valueEncoders.entrySet()) {
            Encoder valueEncoder = entry.getValue();
            Object value = values.get(entry.getKey());
            if (value == null) {
                throw AgException.badRequest("Missing value for compound ID property: %s", entry.getKey());
            }
            valueEncoder.encode(entry.getKey(), value, out);
        }

        out.writeEndObject();
    }
}
