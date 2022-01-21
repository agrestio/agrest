package io.agrest.converter.jsonvalue;

import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.HashMap;
import java.util.Map;

import static io.agrest.reflect.Types.typeForName;

/**
 * @since 5.0
 */
public class JsonValueConvertersProvider implements Provider<JsonValueConverters> {

    private final Map<String, JsonValueConverter<?>> converters;

    public JsonValueConvertersProvider(@Inject Map<String, JsonValueConverter<?>> converters) {
        this.converters = converters;
    }

    @Override
    public JsonValueConverters get() throws DIRuntimeException {
        Map<Class<?>, JsonValueConverter<?>> converters = new HashMap<>();
        this.converters.forEach((k, v) -> converters.put(typeForName(k), v));
        return new JsonValueConverters(converters, defaultConverter());
    }

    protected JsonValueConverter<?> defaultConverter() {
        return GenericConverter.converter();
    }
}
