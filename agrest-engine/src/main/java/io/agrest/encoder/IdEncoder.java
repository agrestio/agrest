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
    public boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException {
        if (object == null) {

            if (propertyName != null) {
                out.writeFieldName(propertyName);
            }
            out.writeNull();
            return true;
        } else {
            return encodeId(propertyName, (Map<String, Object>) object, out);
        }
    }

    protected boolean encodeId(String propertyName, Map<String, Object> id, JsonGenerator out) throws IOException {
        return isCompoundId
                ? encodeCompoundId(propertyName, id, valueEncoders, out)
                : encodeSingleId(propertyName, id, valueEncoder, out);
    }

    private boolean encodeSingleId(String propertyName, Map<String, Object> values,
                                   Encoder valueEncoder, JsonGenerator out) throws IOException {

        if (values.size() != 1) {
            throw new IllegalArgumentException("Can't serialize multi-value ObjectId: " + values);
        }

        Object value = values.entrySet().iterator().next().getValue();
        return valueEncoder.encode(propertyName, value, out);
    }

    private boolean encodeCompoundId(String propertyName, Map<String, Object> values,
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
        return true;
    }
}
