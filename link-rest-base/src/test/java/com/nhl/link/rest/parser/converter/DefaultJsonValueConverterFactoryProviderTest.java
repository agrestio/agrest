package com.nhl.link.rest.parser.converter;

import com.nhl.link.rest.runtime.parser.converter.DefaultJsonValueConverterFactoryProvider;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import org.junit.Test;
import org.mockito.Mockito;

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
        assertSame(customConverter, createFactory(injected).converter(CustomType.class));
    }

    @Test
    public void testConverter_LongKnown() {
        assertSame(LongConverter.converter(), createFactory().converter(Long.class));
        assertSame(LongConverter.converter(), createFactory().converter(Long.TYPE));
    }

    @Test
    public void testConverter_LocalDateKnown() {
        JsonValueConverter<?> converter = createFactory().converter(LocalDate.class);
        assertEquals(ISOLocalDateConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_LocalTimeKnown() {
        JsonValueConverter<?> converter = createFactory().converter(LocalTime.class);
        assertEquals(ISOLocalTimeConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_LocalDateTimeKnown() {
        JsonValueConverter<?> converter = createFactory().converter(LocalDateTime.class);
        assertEquals(ISOLocalDateTimeConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_javaUtilDateKnown() {
        JsonValueConverter<?> converter = createFactory().converter(java.util.Date.class);
        assertEquals(UtcDateConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_javaSqlDateKnown() {
        JsonValueConverter<?> converter = createFactory().converter(java.sql.Date.class);
        assertEquals(UtcDateConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_javaSqlTimeKnown() {
        JsonValueConverter<?> converter = createFactory().converter(java.sql.Time.class);
        assertEquals(UtcDateConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_javaSqlTimestampKnown() {
        JsonValueConverter<?> converter = createFactory().converter(java.sql.Timestamp.class);
        assertEquals(UtcDateConverter.class, converter.getClass());
    }

    public static class CustomType  {

    }
}
