package io.agrest.converter.valuestring;

import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.HashMap;
import java.util.Map;

import static io.agrest.reflect.Types.typeForName;

/**
 * @since 2.11
 */
public class ValueStringConvertersProvider implements Provider<ValueStringConverters> {

    private Map<String, ValueStringConverter> converters;

    public ValueStringConvertersProvider(@Inject Map<String, ValueStringConverter> converters) {
        this.converters = converters;
    }

    @Override
    public ValueStringConverters get() throws DIRuntimeException {
        Map<Class<?>, ValueStringConverter> converters = new HashMap<>();
        this.converters.forEach((k, v) -> converters.put(typeForName(k), v));

        return new ValueStringConverters(converters, defaultConverter());
    }

    /**
     * @since 2.11
     */
    protected ValueStringConverter defaultConverter() {
        return GenericConverter.converter();
    }
}
