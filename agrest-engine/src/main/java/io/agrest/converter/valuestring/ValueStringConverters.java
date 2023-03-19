package io.agrest.converter.valuestring;

import java.util.Map;

/**
 * Provides access to {@link ValueStringConverter} objects by Java type. Can be preconfigured to use custom converters
 * via DI.
 *
 * @since 5.0
 */
public class ValueStringConverters {

    private final Map<Class<?>, ValueStringConverter<?>> convertersByJavaType;
    private final ValueStringConverter<Object> defaultConverter;

    public ValueStringConverters(
            Map<Class<?>, ValueStringConverter<?>> knownConverters,
            ValueStringConverter defaultConverter) {

        this.convertersByJavaType = knownConverters;
        this.defaultConverter = defaultConverter;
    }

    public Map<Class<?>, ValueStringConverter<?>> getConverters() {
        return convertersByJavaType;
    }

    public <T> ValueStringConverter getConverter(Class<? extends T> type) {
        return convertersByJavaType.getOrDefault(type, defaultConverter);
    }
}
