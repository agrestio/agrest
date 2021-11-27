package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.ResourceEntity;
import io.agrest.filter.SelectFilter;

import java.io.IOException;

/**
 * An interceptor for custom encoding of specific entities. An application may define a filter that suppresses
 * certain objects based on security constraints, or may provide a custom encoder for a given object, etc.
 *
 * @deprecated since 4.8 in favor of {@link SelectFilter}.
 */
@Deprecated
public interface EntityEncoderFilter {

    /**
     * @since 3.4
     */
    static EntityEncoderFilterBuilder forAll() {
        return new EntityEncoderFilterBuilder();
    }

    /**
     * @since 3.4
     */
    static EntityEncoderFilterBuilder forEntity(Class<?> entity) {
        return new EntityEncoderFilterBuilder().forEntity(entity);
    }

    /**
     * @since 3.4
     */
    static EntityEncoderFilterBuilder forEntityCondition(EncoderEntityCondition condition) {
        return new EntityEncoderFilterBuilder().entityCondition(condition);
    }

    /**
     * Returns whether the filter should be applied for a given {@link ResourceEntity}.
     */
    boolean matches(ResourceEntity<?> entity);

    boolean encode(String propertyName, Object object, JsonGenerator out, Encoder delegate) throws IOException;

    boolean willEncode(String propertyName, Object object, Encoder delegate);
}
