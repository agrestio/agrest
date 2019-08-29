package io.agrest.sencha.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.EntityProperty;
import io.agrest.encoder.Encoder;

import java.io.IOException;

public abstract class SenchaEntityToOneEncoder implements Encoder {

    private Encoder objectEncoder;
    private EntityProperty idEncoder;

    public SenchaEntityToOneEncoder(Encoder objectEncoder, EntityProperty idEncoder) {
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

	protected abstract String idPropertyName(String propertyName);

}
