package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.EntityProperty;
import io.agrest.PathConstants;

import java.io.IOException;
import java.util.Map;

public class EntityEncoder extends EntityNoIdEncoder {

    private EntityProperty idEncoder;

    public EntityEncoder(
            EntityProperty idEncoder,
            Map<String, EntityProperty> attributeEncoders,
            Map<String, EntityProperty> relationshipEncoders,
            Map<String, EntityProperty> extraEncoders) {

        super(attributeEncoders, relationshipEncoders, extraEncoders);
        this.idEncoder = idEncoder;
    }

    protected void encodeProperties(Object object, JsonGenerator out) throws IOException {
        idEncoder.encode(object, PathConstants.ID_PK_ATTRIBUTE, out);
        super.encodeProperties(object, out);
    }
}
