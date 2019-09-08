package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.EntityProperty;
import io.agrest.PathConstants;

import java.io.IOException;
import java.util.Map;

public class EntityEncoder extends EntityNoIdEncoder {

    private EntityProperty idProperty;

    public EntityEncoder(
            EntityProperty idProperty,
            Map<String, EntityProperty> attributeEncoders,
            Map<String, EntityProperty> relationshipEncoders,
            Map<String, EntityProperty> extraEncoders) {

        super(attributeEncoders, relationshipEncoders, extraEncoders);
        this.idProperty = idProperty;
    }

    protected void encodeProperties(Object object, JsonGenerator out) throws IOException {
        encodeId(object, out);
        super.encodeProperties(object, out);
    }

    protected void encodeId(Object object, JsonGenerator out) throws IOException {
        Object v = object == null ? null : idProperty.getReader().value(object, PathConstants.ID_PK_ATTRIBUTE);
        idProperty.getEncoder().encode(PathConstants.ID_PK_ATTRIBUTE, v, out);
    }
}
