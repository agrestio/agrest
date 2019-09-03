package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.ResourceEntity;

import java.io.IOException;

/**
 * An interceptor for custom encoding of specific entities. An application may define a filter that suppresses
 * certain objects based on security constraints, or may provide a custom encoder for a given object, etc.
 */
public interface EncoderFilter {

	/**
	 * @since 3.4
	 */
    static EncoderFilterBuilder forAll() {
        return new EncoderFilterBuilder();
    }

	/**
	 * @since 3.4
	 */
    static EncoderFilterBuilder forEntity(Class<?> entity) {
        return new EncoderFilterBuilder().forEntity(entity);
    }

	/**
	 * @since 3.4
	 */
    static EncoderFilterBuilder forEntityCondition(EncoderFilterEntityCondition condition) {
        return new EncoderFilterBuilder().entityCondition(condition);
    }

    /**
     * Returns whether the filter should be applied for a given {@link ResourceEntity}.
     */
    boolean matches(ResourceEntity<?> entity);

    boolean encode(String propertyName, Object object, JsonGenerator out, Encoder delegate) throws IOException;

    boolean willEncode(String propertyName, Object object, Encoder delegate);

}
