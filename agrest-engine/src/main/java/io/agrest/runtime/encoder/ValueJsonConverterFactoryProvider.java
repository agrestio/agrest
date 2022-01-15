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

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.agrest.reflect.Types.typeForName;

/**
 * @since 2.11
 */
public class ValueJsonConverterFactoryProvider implements Provider<IValueJsonConverterFactory> {

    private Map<String, ValueJsonConverter> injectedConverters;

    public ValueJsonConverterFactoryProvider(@Inject Map<String, ValueJsonConverter> injectedConverters) {
        this.injectedConverters = injectedConverters;
    }

    @Override
    public IValueJsonConverterFactory get() throws DIRuntimeException {
        Map<Class<?>, ValueJsonConverter> converters = new HashMap<>();
        appendKnownConverters(converters);
        appendInjectedConverters(converters);
        return createFactory(converters, defaultConverter());
    }

    /**
     * @since 2.11
     */
    protected IValueJsonConverterFactory createFactory(
            Map<Class<?>, ValueJsonConverter> converters, ValueJsonConverter defaultConverter) {
        return new ValueJsonConverterFactory(converters, defaultConverter);
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
    protected void appendKnownConverters(Map<Class<?>, ValueJsonConverter> converters) {
        converters.put(LocalDate.class, ISOLocalDateConverter.converter());
        converters.put(LocalTime.class, ISOLocalTimeConverter.converter());
        converters.put(LocalDateTime.class, ISOLocalDateTimeConverter.converter());
        converters.put(OffsetDateTime.class, ISOOffsetDateTimeConverter.converter());
        converters.put(java.util.Date.class, ISODateTimeConverter.converter());
        converters.put(Timestamp.class, ISODateTimeConverter.converter());
        converters.put(java.sql.Date.class, ISODateConverter.converter());
        converters.put(Time.class, ISOTimeConverter.converter());
    }

    protected void appendInjectedConverters(Map<Class<?>, ValueJsonConverter> converters) {
        injectedConverters.forEach((k, v) -> converters.put(typeForName(k), v));
    }
}
