package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.PathConstants;

import java.io.IOException;
import java.util.Map;

public class EntityEncoder extends EntityNoIdEncoder {

    private final EncodableProperty idProperty;

    public EntityEncoder(EncodableProperty idProperty, Map<String, EncodableProperty> encoders) {
        super(encoders);
        this.idProperty = idProperty;
    }

    @Override
    protected void encodeProperties(Object object, boolean skipNullProperties, JsonGenerator out) throws IOException {
        encodeId(object, skipNullProperties, out);
        super.encodeProperties(object, skipNullProperties, out);
    }

    protected void encodeId(Object object, boolean skipNullProperties, JsonGenerator out) throws IOException {
        Object v = object == null ? null : idProperty.getReader().read(object);
        idProperty.getEncoder().encode(PathConstants.ID_PK_ATTRIBUTE, v, skipNullProperties, out);
    }
}
