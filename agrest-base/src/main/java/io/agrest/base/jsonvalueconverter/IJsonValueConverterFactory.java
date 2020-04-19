package io.agrest.base.jsonvalueconverter;

import io.agrest.base.jsonvalueconverter.JsonValueConverter;

import java.lang.reflect.Type;
import java.util.Optional;

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

    <T> Optional<JsonValueConverter<T>> typedConverter(Class<T> valueType);
}
