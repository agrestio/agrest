package com.nhl.link.rest.runtime.parser.converter;

import com.nhl.link.rest.parser.converter.Base64Converter;
import com.nhl.link.rest.parser.converter.FloatConverter;
import com.nhl.link.rest.parser.converter.GenericConverter;
import com.nhl.link.rest.parser.converter.ISOLocalDateConverter;
import com.nhl.link.rest.parser.converter.ISOLocalDateTimeConverter;
import com.nhl.link.rest.parser.converter.ISOLocalTimeConverter;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import com.nhl.link.rest.parser.converter.LongConverter;
import com.nhl.link.rest.parser.converter.UtcDateConverter;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Provider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 2.10
 */
public class DefaultJsonValueConverterFactoryProvider implements Provider<IJsonValueConverterFactory> {

    @Override
    public IJsonValueConverterFactory get() throws DIRuntimeException {
        return new DefaultJsonValueConverterFactory(createKnownConverters(), GenericConverter.converter());
    }

    protected Map<Class<?>, JsonValueConverter> createKnownConverters() {
        Map<Class<?>, JsonValueConverter> knownConverters = new HashMap<>();

        knownConverters.put(Object.class, GenericConverter.converter());
        knownConverters.put(Float.class, FloatConverter.converter());
        knownConverters.put(float.class, FloatConverter.converter());
        knownConverters.put(Long.class, LongConverter.converter());
        knownConverters.put(long.class, LongConverter.converter());
        knownConverters.put(Date.class, UtcDateConverter.converter());
        knownConverters.put(java.sql.Date.class, UtcDateConverter.converter());
        knownConverters.put(java.sql.Time.class, UtcDateConverter.converter());
        knownConverters.put(java.sql.Timestamp.class, UtcDateConverter.converter());
        knownConverters.put(byte[].class, Base64Converter.converter());
        knownConverters.put(LocalDate.class, ISOLocalDateConverter.converter());
        knownConverters.put(LocalTime.class, ISOLocalTimeConverter.converter());
        knownConverters.put(LocalDateTime.class, ISOLocalDateTimeConverter.converter());

        return knownConverters;
    }
}
