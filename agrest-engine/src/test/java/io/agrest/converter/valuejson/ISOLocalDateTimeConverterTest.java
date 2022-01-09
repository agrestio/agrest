package io.agrest.converter.valuejson;

import io.agrest.converter.valuejson.ISOLocalDateTimeConverter;
import io.agrest.converter.valuejson.ValueJsonConverter;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ISOLocalDateTimeConverterTest {

    private ValueJsonConverter converter = ISOLocalDateTimeConverter.converter();

    @Test
    public void testISOLocalDateTimeConverter() {
        _testISOLocalDateTimeConverter(1458995247000L, "yyyy-MM-dd'T'HH:mm:ss");
    }

    @Test
    public void testISOLocalDateTimeConverter_FractionalPart1() {
        _testISOLocalDateTimeConverter(1458995247001L, "yyyy-MM-dd'T'HH:mm:ss.SSS");
    }

    @Test
    public void testISOLocalDateTimeConverter_FractionalPart2() {
        _testISOLocalDateTimeConverter(1458995247100L, "yyyy-MM-dd'T'HH:mm:ss.S");
    }

    /**
     * Prints java.util.Date in the server's local timezone
     */
    private void _testISOLocalDateTimeConverter(long millis, String expectedPattern) {
        LocalDateTime dateTime = fromMillis(millis);
        String expected = DateTimeFormatter.ofPattern(expectedPattern).format(dateTime);
        assertEquals(expected, converter.asString(dateTime));
    }

    private static LocalDateTime fromMillis(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }
}
