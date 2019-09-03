package io.agrest.sencha.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.EntityProperty;
import io.agrest.encoder.Encoder;

import java.io.IOException;

public class SenchaEntityToOneEncoder implements Encoder {

    private Encoder objectEncoder;
    private EntityProperty idEncoder;
    private String idPropertyName;


    public SenchaEntityToOneEncoder(String idPropertyName, Encoder objectEncoder, EntityProperty idEncoder) {
        this.idPropertyName = idPropertyName;
        this.objectEncoder = objectEncoder;
        this.idEncoder = idEncoder;
    }

    @Override
    public boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException {
        if (!objectEncoder.encode(propertyName, object, out)) {
            return false;
        }

        // encode FK as 'xyz_id' property
        if (propertyName != null) {
            String idPropertyName = idPropertyName(propertyName);
            idEncoder.encode(object, idPropertyName, out);
        }

        return true;
    }

    @Override
    public boolean willEncode(String propertyName, Object object) {
        return true;
    }

    protected String idPropertyName(String propertyName) {
        // we know that created encoder will only be used for encoding a
        // single known property, so hardcode the ID property to avoid
        // relationshipMapper lookups in a loop
        return idPropertyName;
    }
}
