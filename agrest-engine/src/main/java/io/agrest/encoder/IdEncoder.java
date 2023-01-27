package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.AgException;

import java.io.IOException;
import java.util.Map;

/**
 * @since 1.2
 */
public class IdEncoder extends AbstractEncoder {

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


    @Override
    protected void encodeNonNullObject(Object object, boolean skipNullProperties, JsonGenerator out) throws IOException {
        if (isCompoundId) {
            encodeCompoundId((Map<String, Object>) object, valueEncoders, skipNullProperties, out);
        } else {
            encodeSingleId((Map<String, Object>) object, valueEncoder, skipNullProperties, out);
        }
    }

    private void encodeSingleId(
            Map<String, Object> values, Encoder valueEncoder, boolean skipNullProperties, JsonGenerator out) throws IOException {

        if (values.size() != 1) {
            throw new IllegalArgumentException("Can't serialize multi-value ObjectId: " + values);
        }

        Object value = values.entrySet().iterator().next().getValue();
        valueEncoder.encode(null, value, skipNullProperties, out);
    }

    private void encodeCompoundId(
            Map<String, Object> values, Map<String, Encoder> valueEncoders, boolean skipNullProperties, JsonGenerator out) throws IOException {

        out.writeStartObject();

        for (Map.Entry<String, Encoder> entry : valueEncoders.entrySet()) {
            Encoder valueEncoder = entry.getValue();
            Object value = values.get(entry.getKey());
            if (value == null) {
                throw AgException.badRequest("Missing value for compound ID property: %s", entry.getKey());
            }
            valueEncoder.encode(entry.getKey(), value, skipNullProperties, out);
        }

        out.writeEndObject();
    }
}
