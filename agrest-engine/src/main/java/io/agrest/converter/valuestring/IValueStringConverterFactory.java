package io.agrest.converter.valuestring;

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
}
