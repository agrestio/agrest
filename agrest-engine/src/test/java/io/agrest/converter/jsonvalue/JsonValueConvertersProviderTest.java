package io.agrest.converter.jsonvalue;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

public class JsonValueConvertersProviderTest {

    public JsonValueConverters createConverters() {
        return createConverters(Collections.emptyMap());
    }

    public JsonValueConverters createConverters(Map<String, JsonValueConverter<?>> injected) {
        return new JsonValueConvertersProvider(injected).get();
    }

    @Test
    public void testConverter_Injected() {

        JsonValueConverter<?> customConverter = mock(JsonValueConverter.class);
        Map<String, JsonValueConverter<?>> injected = Collections.singletonMap(CustomType.class.getName(), customConverter);
        assertSame(customConverter, createConverters(injected).converter(CustomType.class));
    }

    @Test
    public void testConverter_Generic() {
        assertSame(GenericConverter.converter(), createConverters().converter(this.getClass()));
        assertSame(GenericConverter.converter(), createConverters().converter(Object.class));
    }

    @Test
    public void testConverter_LongKnown() {
        assertSame(LongConverter.converter(), createConverters().converter(Long.class));
        assertSame(LongConverter.converter(), createConverters().converter(Long.TYPE));
    }

    @Test
    public void testConverter_LocalDateKnown() {
        JsonValueConverter<?> converter = createConverters().converter(LocalDate.class);
        assertEquals(ISOLocalDateConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_LocalTimeKnown() {
        JsonValueConverter<?> converter = createConverters().converter(LocalTime.class);
        assertEquals(ISOLocalTimeConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_LocalDateTimeKnown() {
        JsonValueConverter<?> converter = createConverters().converter(LocalDateTime.class);
        assertEquals(ISOLocalDateTimeConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_javaUtilDateKnown() {
        JsonValueConverter<?> converter = createConverters().converter(java.util.Date.class);
        assertEquals(UtcDateConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_javaSqlDateKnown() {
        JsonValueConverter<?> converter = createConverters().converter(java.sql.Date.class);
        assertEquals(UtcDateConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_javaSqlTimeKnown() {
        JsonValueConverter<?> converter = createConverters().converter(java.sql.Time.class);
        assertEquals(UtcDateConverter.class, converter.getClass());
    }

    @Test
    public void testConverter_javaSqlTimestampKnown() {
        JsonValueConverter<?> converter = createConverters().converter(java.sql.Timestamp.class);
        assertEquals(UtcDateConverter.class, converter.getClass());
    }

    public static class CustomType {

    }
}
