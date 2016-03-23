package com.nhl.link.rest.runtime.adapter.sencha;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.EntityToOneEncoder;

public abstract class SenchaEntityToOneEncoder extends EntityToOneEncoder {

	private EntityProperty idEncoder;

	public SenchaEntityToOneEncoder(Encoder objectEncoder, EntityProperty idEncoder) {
		super(objectEncoder);
		this.idEncoder = idEncoder;
	}

	@Override
	public boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException {
		if (!super.encode(propertyName, object, out)) {
			return false;
		}

		// encode FK as 'xyz_id' property
		if (propertyName != null) {
			String idPropertyName = idPropertyName(propertyName);
			idEncoder.encode(object, idPropertyName, out);
		}

		return true;
	}

	protected abstract String idPropertyName(String propertyName);

}
