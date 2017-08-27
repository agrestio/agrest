package com.nhl.link.rest.runtime.parser.converter;

import com.nhl.link.rest.parser.converter.EnumConverter;
import com.nhl.link.rest.parser.converter.JsonValueConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 1.10
 */
public class DefaultJsonValueConverterFactory implements IJsonValueConverterFactory {

    protected Map<Class<?>, JsonValueConverter> convertersByJavaType;

    private JsonValueConverter defaultConverter;

    public DefaultJsonValueConverterFactory(
            Map<Class<?>, JsonValueConverter> knownConverters,
            JsonValueConverter defaultConverter) {

        this.defaultConverter = defaultConverter;

        // creating a concurrent copy of the provided map - we'll be expanding it dynamically.
        this.convertersByJavaType = new ConcurrentHashMap<>(knownConverters);
    }

    @Override
    public JsonValueConverter converter(Class<?> valueType) {
        return convertersByJavaType.computeIfAbsent(valueType, vt ->
                (vt.isEnum()) ? new EnumConverter((Class<? extends Enum>) vt) : defaultConverter);
    }
}
