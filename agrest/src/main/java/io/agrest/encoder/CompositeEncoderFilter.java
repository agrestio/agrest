package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.ResourceEntity;

import java.io.IOException;

/**
 * Encoder filter that delegates individual encoder methods to custom functions.
 *
 * @since 3.4
 */
class CompositeEncoderFilter implements EncoderFilter {

    private static final EncoderFilterEntityCondition DEFAUL_ENTITY_CONDITION = e -> true;
    private static final EncoderFilterObjectCondition<Object> DEFAULT_OBJECT_CONDITION = (p, o, d) -> d.willEncode(p, o);
    private static final EncoderFilterEncoder<Object> DEFAULT_ENCODER = (p, o, out, d) -> d.encode(p, o, out);

    EncoderFilterEntityCondition entityCondition;
    EncoderFilterObjectCondition objectCondition;
    EncoderFilterEncoder encoder;

    CompositeEncoderFilter() {
        this.entityCondition = DEFAUL_ENTITY_CONDITION;
        this.objectCondition = DEFAULT_OBJECT_CONDITION;
        this.encoder = DEFAULT_ENCODER;
    }

    @Override
    public boolean matches(ResourceEntity<?> entity) {
        return entityCondition.test(entity);
    }

    @Override
    public boolean willEncode(String propertyName, Object object, Encoder delegate) {
        return objectCondition.test(propertyName, object, delegate);
    }

    @Override
    public boolean encode(String propertyName, Object object, JsonGenerator out, Encoder delegate) throws IOException {
        return encoder.encode(propertyName, object, out, delegate);
    }
}
