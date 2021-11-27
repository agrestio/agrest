package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.ResourceEntity;
import io.agrest.filter.ReadFilter;

import java.io.IOException;

/**
 * Encoder filter that delegates individual encoder methods to custom functions.
 *
 * @since 3.4
 * @deprecated since 4.8 in favor of {@link ReadFilter}.
 */
@Deprecated
class CompositeEntityEncoderFilter implements EntityEncoderFilter {

    private static final EncoderEntityCondition DEFAUL_ENTITY_CONDITION = e -> true;
    private static final EncoderObjectCondition<Object> DEFAULT_OBJECT_CONDITION = (p, o, d) -> d.willEncode(p, o);
    private static final EncoderMethod<Object> DEFAULT_ENCODER = (p, o, out, d) -> d.encode(p, o, out);

    EncoderEntityCondition entityCondition;
    EncoderObjectCondition objectCondition;
    EncoderMethod encoder;

    CompositeEntityEncoderFilter() {
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
