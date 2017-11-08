package com.nhl.link.rest.runtime.encoder;

import com.nhl.link.rest.encoder.converter.GenericConverter;
import com.nhl.link.rest.encoder.converter.ISODateConverter;
import com.nhl.link.rest.encoder.converter.ISODateTimeConverter;
import com.nhl.link.rest.encoder.converter.ISOLocalDateConverter;
import com.nhl.link.rest.encoder.converter.ISOLocalDateTimeConverter;
import com.nhl.link.rest.encoder.converter.ISOLocalTimeConverter;
import com.nhl.link.rest.encoder.converter.ISOTimeConverter;
import com.nhl.link.rest.encoder.converter.StringConverter;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.HashMap;
import java.util.Map;

import static com.nhl.link.rest.meta.Types.typeForName;

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
        converters.put(AttributeEncoderFactory.LOCAL_DATE, ISOLocalDateConverter.converter());
        converters.put(AttributeEncoderFactory.LOCAL_TIME, ISOLocalDateTimeConverter.converter());
        converters.put(AttributeEncoderFactory.LOCAL_DATETIME, ISOLocalTimeConverter.converter());
        converters.put(AttributeEncoderFactory.UTIL_DATE, ISODateTimeConverter.converter());
        converters.put(AttributeEncoderFactory.SQL_TIMESTAMP, ISODateTimeConverter.converter());
        converters.put(AttributeEncoderFactory.SQL_DATE, ISODateConverter.converter());
        converters.put(AttributeEncoderFactory.SQL_TIME, ISOTimeConverter.converter());

        return converters;
    }

    protected Map<Class<?>, StringConverter> appendInjectedConverters(Map<Class<?>, StringConverter> converters) {
        injectedConverters.forEach((k, v) -> converters.put(typeForName(k), v));
        return converters;
    }
}
