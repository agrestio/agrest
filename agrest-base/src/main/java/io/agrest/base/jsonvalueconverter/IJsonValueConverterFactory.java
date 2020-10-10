package io.agrest.base.jsonvalueconverter;

import java.lang.reflect.Type;

/**
 * A service that ensures proper conversion of incoming JSON values to the
 * model-compatible Java types.
 *
 * @since 1.10
 */
public interface IJsonValueConverterFactory {

    /**
     * @since 1.24
     */
    JsonValueConverter<?> converter(Type valueType);

    <T> JsonValueConverter<T> typedConverter(Class<T> valueType);
}
