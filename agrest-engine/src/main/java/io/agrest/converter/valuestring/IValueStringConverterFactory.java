package io.agrest.converter.valuestring;

import io.agrest.meta.AgEntity;

import java.util.Map;

/**
 * A strategy for converting various types of properties to String values. Used for things like "mapBy", where property
 * value is a JSON key, and hence must be a String.
 *
 * @since 5.0
 */
public interface IValueStringConverterFactory {

    /**
     * Returns all registered converters by Java type.
     */
    Map<Class<?>, ValueStringConverter> getConverters();

    ValueStringConverter getConverter(Class<?> type);

    /**
     * Returns a {@link ValueStringConverter} for a given entity object. Normally the returned converter is some kind of
     * ID converter.
     */
    ValueStringConverter getConverter(AgEntity<?> entity);

    /**
     * Returns a {@link ValueStringConverter} for a given attribute.
     */
    ValueStringConverter getConverter(AgEntity<?> entity, String attributeName);

}
