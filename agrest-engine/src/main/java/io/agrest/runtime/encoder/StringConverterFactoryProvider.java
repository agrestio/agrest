package io.agrest.runtime.encoder;

import io.agrest.converter.valuejson.GenericConverter;
import io.agrest.converter.valuejson.ISODateConverter;
import io.agrest.converter.valuejson.ISODateTimeConverter;
import io.agrest.converter.valuejson.ISOLocalDateConverter;
import io.agrest.converter.valuejson.ISOLocalDateTimeConverter;
import io.agrest.converter.valuejson.ISOLocalTimeConverter;
import io.agrest.converter.valuejson.ISOOffsetDateTimeConverter;
import io.agrest.converter.valuejson.ISOTimeConverter;
import io.agrest.converter.valuejson.ValueJsonConverter;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.HashMap;
import java.util.Map;

import static io.agrest.reflect.Types.typeForName;

/**
 * @since 2.11
 */
public class StringConverterFactoryProvider implements Provider<IStringConverterFactory> {

    private Map<String, ValueJsonConverter> injectedConverters;

    public StringConverterFactoryProvider(@Inject Map<String, ValueJsonConverter> injectedConverters) {
        this.injectedConverters = injectedConverters;
    }

    @Override
    public IStringConverterFactory get() throws DIRuntimeException {
        Map<Class<?>, ValueJsonConverter> converters =
                appendInjectedConverters(
                        appendKnownConverters(new HashMap<>()));

        return createFactory(converters, defaultConverter());
    }

    /**
     * @since 2.11
     */
    protected IStringConverterFactory createFactory(Map<Class<?>, ValueJsonConverter> converters,
                                                     ValueJsonConverter defaultConverter) {
        return new StringConverterFactory(converters, defaultConverter);
    }

    /**
     * @since 2.11
     */
    protected ValueJsonConverter defaultConverter() {
        return GenericConverter.converter();
    }

    /**
     * @since 2.11
     */
    protected Map<Class<?>, ValueJsonConverter> appendKnownConverters(Map<Class<?>, ValueJsonConverter> converters) {
        converters.put(PropertyTypes.LOCAL_DATE, ISOLocalDateConverter.converter());
        converters.put(PropertyTypes.LOCAL_TIME, ISOLocalDateTimeConverter.converter());
        converters.put(PropertyTypes.LOCAL_DATETIME, ISOLocalTimeConverter.converter());
        converters.put(PropertyTypes.OFFSET_DATETIME, ISOOffsetDateTimeConverter.converter());
        converters.put(PropertyTypes.UTIL_DATE, ISODateTimeConverter.converter());
        converters.put(PropertyTypes.SQL_TIMESTAMP, ISODateTimeConverter.converter());
        converters.put(PropertyTypes.SQL_DATE, ISODateConverter.converter());
        converters.put(PropertyTypes.SQL_TIME, ISOTimeConverter.converter());

        return converters;
    }

    protected Map<Class<?>, ValueJsonConverter> appendInjectedConverters(Map<Class<?>, ValueJsonConverter> converters) {
        injectedConverters.forEach((k, v) -> converters.put(typeForName(k), v));
        return converters;
    }
}
