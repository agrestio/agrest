package io.agrest.runtime.encoder;

import io.agrest.converter.valuejson.ValueJsonConverter;
import io.agrest.meta.AgEntity;

import java.util.Map;

/**
 * A strategy for converting various types of properties to String values. Used for things like "mapBy", where property
 * value is a JSON key, and hence must be a String.
 *
 * @since 5.0
 */
public interface IValueJsonConverterFactory {

    /**
     * Returns all registered converters by Java type.
     */
    Map<Class<?>, ValueJsonConverter> getConverters();

    ValueJsonConverter getConverter(Class<?> type);

    /**
     * Returns a {@link ValueJsonConverter} for a given entity object. Normally the returned converter is some kind of
     * ID converter.
     */
    ValueJsonConverter getConverter(AgEntity<?> entity);

    /**
     * Returns a {@link ValueJsonConverter} for a given attribute.
     */
    ValueJsonConverter getConverter(AgEntity<?> entity, String attributeName);

}
