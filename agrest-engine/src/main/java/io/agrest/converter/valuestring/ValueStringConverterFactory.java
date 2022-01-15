package io.agrest.converter.valuestring;

import java.util.Map;

/**
 * @since 5.0
 */
public class ValueStringConverterFactory implements IValueStringConverterFactory {

    private final Map<Class<?>, ValueStringConverter> convertersByJavaType;
    private final ValueStringConverter defaultConverter;

    public ValueStringConverterFactory(
            Map<Class<?>, ValueStringConverter> knownConverters,
            ValueStringConverter defaultConverter) {

        this.convertersByJavaType = knownConverters;
        this.defaultConverter = defaultConverter;
    }

    @Override
    public Map<Class<?>, ValueStringConverter> getConverters() {
        return convertersByJavaType;
    }

    @Override
    public ValueStringConverter getConverter(Class<?> type) {
        return convertersByJavaType.getOrDefault(type, defaultConverter);
    }
}
