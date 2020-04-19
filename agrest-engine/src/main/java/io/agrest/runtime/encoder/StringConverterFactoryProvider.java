package io.agrest.runtime.encoder;

import io.agrest.encoder.converter.GenericConverter;
import io.agrest.encoder.converter.ISODateConverter;
import io.agrest.encoder.converter.ISODateTimeConverter;
import io.agrest.encoder.converter.ISOLocalDateConverter;
import io.agrest.encoder.converter.ISOLocalDateTimeConverter;
import io.agrest.encoder.converter.ISOLocalTimeConverter;
import io.agrest.encoder.converter.ISOOffsetDateTimeConverter;
import io.agrest.encoder.converter.ISOTimeConverter;
import io.agrest.encoder.converter.StringConverter;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.HashMap;
import java.util.Map;

import static io.agrest.base.reflect.Types.typeForName;

/**
 * @since 2.11
 */
public class StringConverterFactoryProvider implements Provider<IStringConverterFactory> {

    private Map<String, StringConverter> injectedConverters;

    public StringConverterFactoryProvider(@Inject Map<String, StringConverter> injectedConverters) {
        this.injectedConverters = injectedConverters;
    }

    @Override
    public IStringConverterFactory get() throws DIRuntimeException {
        Map<Class<?>, StringConverter> converters =
                appendInjectedConverters(
                        appendKnownConverters(new HashMap<>()));

        return createFactory(converters, defaultConverter());
    }

    /**
     * @since 2.11
     */
    protected IStringConverterFactory createFactory(Map<Class<?>, StringConverter> converters,
                                                     StringConverter defaultConverter) {
        return new StringConverterFactory(converters, defaultConverter);
    }

    /**
     * @since 2.11
     */
    protected StringConverter defaultConverter() {
        return GenericConverter.converter();
    }

    /**
     * @since 2.11
     */
    protected Map<Class<?>, StringConverter> appendKnownConverters(Map<Class<?>, StringConverter> converters) {
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

    protected Map<Class<?>, StringConverter> appendInjectedConverters(Map<Class<?>, StringConverter> converters) {
        injectedConverters.forEach((k, v) -> converters.put(typeForName(k), v));
        return converters;
    }
}
