package io.agrest.runtime.parser.converter;

import io.agrest.parser.converter.JsonValueConverter;

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
