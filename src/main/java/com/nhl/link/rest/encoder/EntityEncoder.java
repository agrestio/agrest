package com.nhl.link.rest.encoder;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.runtime.parser.PathConstants;

public class EntityEncoder extends AbstractEncoder {

	private EntityProperty idEncoder;
	private Map<String, EntityProperty> propertyEncoders;

	public EntityEncoder(EntityProperty idEncoder, Map<String, EntityProperty> propertyEncoders) {
		this.idEncoder = idEncoder;
		this.propertyEncoders = propertyEncoders;
	}

	@Override
	protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {

		out.writeStartObject();

		idEncoder.encode(object, PathConstants.ID_PK_ATTRIBUTE, out);

		for (Entry<String, EntityProperty> e : propertyEncoders.entrySet()) {
			e.getValue().encode(object, e.getKey(), out);
		}

		out.writeEndObject();
		return true;
	}
}
