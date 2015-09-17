package com.nhl.link.rest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.LinkRestException;
import org.apache.cayenne.ObjectId;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

/**
 * @since 1.2
 */
public class ObjectIdEncoder implements Encoder {

    private Encoder valueEncoder;
    private Map<String, Encoder> valueEncoders;
    private boolean isCompoundId;

    public ObjectIdEncoder(Encoder valueEncoder) {
        this.valueEncoder = valueEncoder;
    }

    public ObjectIdEncoder(Map<String, Encoder> valueEncoders) {
        this.valueEncoders = valueEncoders;
        isCompoundId = true;
    }

    @Override
    public boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException {
        if (object == null) {

            if (propertyName != null) {
                out.writeFieldName(propertyName);
            }
            out.writeNull();
            return true;
        } else {
            return encodeId(propertyName, (ObjectId) object, out);
        }
    }

    @Override
    public boolean willEncode(String propertyName, Object object) {
        return true;
    }

    protected boolean encodeId(String propertyName, ObjectId id, JsonGenerator out) throws IOException {

        // for now supporting only single-value permanent IDs

        if (id.isTemporary()) {
            throw new IllegalArgumentException("Can't serialize temporary ObjectId: " + id);
        }

        return isCompoundId? encodeCompoundId(propertyName, id, valueEncoders, out)
                : encodeSingleId(propertyName, id, valueEncoder, out);
    }

    private boolean encodeSingleId(String propertyName, ObjectId id,
                                   Encoder valueEncoder, JsonGenerator out) throws IOException {

        Map<String, Object> values = id.getIdSnapshot();

        if (values.size() != 1) {
            throw new IllegalArgumentException("Can't serialize multi-value ObjectId: " + values);
        }

        Object value = values.entrySet().iterator().next().getValue();
        return valueEncoder.encode(propertyName, value, out);
    }

    private boolean encodeCompoundId(String propertyName, ObjectId id,
                                     Map<String, Encoder> valueEncoders, JsonGenerator out) throws IOException {

        Map<String, Object> values = id.getIdSnapshot();

        if (propertyName != null) {
            out.writeFieldName(propertyName);
        }

        out.writeStartObject();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Encoder valueEncoder = valueEncoders.get(entry.getKey());
            if (valueEncoder == null) {
                throw new LinkRestException(Response.Status.BAD_REQUEST,
                        "Missing encoder for compound ID property: " + entry.getKey());
            }
            valueEncoder.encode(entry.getKey(), entry.getValue(), out);
        }

        out.writeEndObject();
        return true;
    }
}
