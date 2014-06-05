package com.nhl.link.rest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.cayenne.ObjectId;

import java.io.IOException;
import java.util.Map;

/**
 * @since 1.2
 */
public class ObjectIdEncoder implements Encoder {

    private Encoder valueEncoder;

    public ObjectIdEncoder(Encoder valueEncoder) {
        this.valueEncoder = valueEncoder;
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

        Map<String, Object> values = id.getIdSnapshot();

        if (values.size() != 1) {
            throw new IllegalArgumentException("Can't serialize multi-value ObjectId: " + values);
        }

        Object value = values.entrySet().iterator().next().getValue();
        return valueEncoder.encode(propertyName, value, out);
    }
}
