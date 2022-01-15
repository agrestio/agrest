package io.agrest.converter.valuestring;

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
public class ValueStringConvertersProvider implements Provider<ValueStringConverters> {

    private Map<String, ValueStringConverter> injectedConverters;

    public ValueStringConvertersProvider(@Inject Map<String, ValueStringConverter> injectedConverters) {
        this.injectedConverters = injectedConverters;
    }

    @Override
    public ValueStringConverters get() throws DIRuntimeException {
        Map<Class<?>, ValueStringConverter> converters = new HashMap<>();
        appendKnownConverters(converters);
        appendInjectedConverters(converters);
        return createConverters(converters, defaultConverter());
    }

    /**
     * @since 2.11
     */
    protected ValueStringConverters createConverters(
            Map<Class<?>, ValueStringConverter> converters, ValueStringConverter defaultConverter) {
        return new ValueStringConverters(converters, defaultConverter);
    }

    /**
     * @since 2.11
     */
    protected ValueStringConverter defaultConverter() {
        return GenericConverter.converter();
    }

    /**
     * @since 2.11
     */
    protected void appendKnownConverters(Map<Class<?>, ValueStringConverter> converters) {
        converters.put(LocalDate.class, ISOLocalDateConverter.converter());
        converters.put(LocalTime.class, ISOLocalTimeConverter.converter());
        converters.put(LocalDateTime.class, ISOLocalDateTimeConverter.converter());
        converters.put(OffsetDateTime.class, ISOOffsetDateTimeConverter.converter());
        converters.put(java.util.Date.class, ISODateTimeConverter.converter());
        converters.put(Timestamp.class, ISODateTimeConverter.converter());
        converters.put(java.sql.Date.class, ISODateConverter.converter());
        converters.put(Time.class, ISOTimeConverter.converter());
    }

    protected void appendInjectedConverters(Map<Class<?>, ValueStringConverter> converters) {
        injectedConverters.forEach((k, v) -> converters.put(typeForName(k), v));
    }
}
