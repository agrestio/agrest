package com.nhl.link.rest.parser.converter;

import com.nhl.link.rest.runtime.parser.converter.DefaultJsonValueConverterFactoryProvider;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class DefaultJsonValueConverterFactoryProviderTest {

    public IJsonValueConverterFactory createFactory() {
        return createFactory(Collections.emptyMap());
    }

    public IJsonValueConverterFactory createFactory(Map<String, JsonValueConverter<?>> injected) {
        return new DefaultJsonValueConverterFactoryProvider(injected).get();
    }

    @Test
    public void testConverter_Injected() {

        JsonValueConverter<?> customConverter = mock(JsonValueConverter.class);
        Map<String, JsonValueConverter<?>> injected = Collections.singletonMap(CustomType.class.getName(), customConverter);
        assertSame(customConverter, createFactory(injected).typedConverter(CustomType.class));
    }

    @Test
    public void testConverter_LongKnown() {
        assertSame(LongConverter.converter(), createFactory().typedConverter(Long.class));
        assertSame(LongConverter.converter(), createFactory().typedConverter(Long.TYPE));
    }

    @Test
    public void testConverter_LocalDateKnown() {
        JsonValueConverter<?> converter = createFactory().typedConverter(LocalDate.class);
        assertEquals(ISOLocalDateConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_LocalTimeKnown() {
        JsonValueConverter<?> converter = createFactory().typedConverter(LocalTime.class);
        assertEquals(ISOLocalTimeConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_LocalDateTimeKnown() {
        JsonValueConverter<?> converter = createFactory().typedConverter(LocalDateTime.class);
        assertEquals(ISOLocalDateTimeConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_javaUtilDateKnown() {
        JsonValueConverter<?> converter = createFactory().typedConverter(java.util.Date.class);
        assertEquals(UtcDateConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_javaSqlDateKnown() {
        JsonValueConverter<?> converter = createFactory().typedConverter(java.sql.Date.class);
        assertEquals(UtcDateConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_javaSqlTimeKnown() {
        JsonValueConverter<?> converter = createFactory().typedConverter(java.sql.Time.class);
        assertEquals(UtcDateConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_javaSqlTimestampKnown() {
        JsonValueConverter<?> converter = createFactory().typedConverter(java.sql.Timestamp.class);
        assertEquals(UtcDateConverter.class, converter.getClass());
    }

    public static class CustomType  {

    }
}
