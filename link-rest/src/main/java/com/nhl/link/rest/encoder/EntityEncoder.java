package com.nhl.link.rest.encoder;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.runtime.parser.PathConstants;

public class EntityEncoder extends AbstractEncoder {

	private EntityProperty idEncoder;
	private Map<String, EntityProperty> relationshipEncoders;
	private Map<String, EntityProperty> combinedEncoders;

	public EntityEncoder(EntityProperty idEncoder, Map<String, EntityProperty> attributeEncoders,
			Map<String, EntityProperty> relationshipEncoders, Map<String, EntityProperty> extraEncoders) {

		this.idEncoder = idEncoder;

		// tracking relationship encoders separately for the sake of the
		// visitors
		this.relationshipEncoders = relationshipEncoders;

		this.combinedEncoders = new TreeMap<>();
		combinedEncoders.putAll(attributeEncoders);
		combinedEncoders.putAll(relationshipEncoders);
		combinedEncoders.putAll(extraEncoders);
	}

	@Override
	protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {

		out.writeStartObject();

		idEncoder.encode(object, PathConstants.ID_PK_ATTRIBUTE, out);

		for (Entry<String, EntityProperty> e : combinedEncoders.entrySet()) {
			e.getValue().encode(object, e.getKey(), out);
		}

		out.writeEndObject();
		return true;
	}

	@Override
	public int visitEntities(Object object, EncoderVisitor visitor) {

		if (object == null || !willEncode(null, object)) {
			return VISIT_CONTINUE;
		}

		int bitmask = visitor.visit(object);

		if ((bitmask & VISIT_SKIP_ALL) != 0) {
			return VISIT_SKIP_ALL;
		}

		if ((bitmask & VISIT_SKIP_CHILDREN) == 0) {

			for (Entry<String, EntityProperty> e : relationshipEncoders.entrySet()) {

				visitor.push(e.getKey());

				try {

					int propBitmask = e.getValue().visit(object, e.getKey(), visitor);

					if ((propBitmask & VISIT_SKIP_ALL) != 0) {
						return VISIT_SKIP_ALL;
					}
				} finally {
					visitor.pop();
				}
			}

		}

		return VISIT_CONTINUE;
	}
}
