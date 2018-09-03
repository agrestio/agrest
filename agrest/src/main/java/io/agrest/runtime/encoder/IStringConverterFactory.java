package io.agrest.runtime.encoder;

import io.agrest.encoder.converter.StringConverter;
import io.agrest.meta.LrEntity;

public interface IStringConverterFactory {

	/**
	 * Returns a {@link StringConverter} for a given entity object. Normally the
	 * returned converter is some kind of ID converter.
	 * 
	 * @since 1.12
	 */
	StringConverter getConverter(LrEntity<?> entity);

	/**
	 * Returns a {@link StringConverter} for a given attribute.
	 * 
	 * @since 1.12
	 */
	StringConverter getConverter(LrEntity<?> entity, String attributeName);

}
