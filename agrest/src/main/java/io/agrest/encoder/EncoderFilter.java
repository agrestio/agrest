package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.ResourceEntity;

import java.io.IOException;

/**
 * Defines API of an interceptor for encoding specific entities. E.g. an application may define a filter that suppresses
 * certain objects based on security constraints. Or provide an alternative encoder for a given object, etc.
 */
public interface EncoderFilter {

	/**
	 * Returns whether the filter should be applied for a given {@link ResourceEntity}.
	 */
	boolean matches(ResourceEntity<?> entity);

	boolean encode(String propertyName, Object object, JsonGenerator out, Encoder delegate) throws IOException;

	boolean willEncode(String propertyName, Object object, Encoder delegate);

}
