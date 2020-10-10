package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.PathConstants;

import java.io.IOException;
import java.util.Map;

public class EntityEncoder extends EntityNoIdEncoder {

    private EncodableProperty idProperty;

    public EntityEncoder(
            EncodableProperty idProperty,
            Map<String, EncodableProperty> attributeEncoders,
            Map<String, EncodableProperty> relationshipEncoders) {

        super(attributeEncoders, relationshipEncoders);
        this.idProperty = idProperty;
    }

    protected void encodeProperties(Object object, JsonGenerator out) throws IOException {
        encodeId(object, out);
        super.encodeProperties(object, out);
    }

    protected void encodeId(Object object, JsonGenerator out) throws IOException {
        Object v = object == null ? null : idProperty.getReader().value(object);
        idProperty.getEncoder().encode(PathConstants.ID_PK_ATTRIBUTE, v, out);
    }
}
