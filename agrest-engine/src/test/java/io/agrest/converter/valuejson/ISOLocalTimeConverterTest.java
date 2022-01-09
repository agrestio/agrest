package io.agrest.converter.valuejson;

import io.agrest.converter.valuejson.ISOLocalTimeConverter;
import io.agrest.converter.valuejson.ValueJsonConverter;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ISOLocalTimeConverterTest {

    private ValueJsonConverter converter = ISOLocalTimeConverter.converter();

    @Test
    public void testISOLocalTimeConverter() {
        _testISOLocalTimeConverter(1458995247000L, "HH:mm:ss");
    }

    @Test
    public void testISOLocalTimeConverter_FractionalPart1() {
        _testISOLocalTimeConverter(1458995247001L, "HH:mm:ss.SSS");
    }

    @Test
    public void testISOLocalTimeConverter_FractionalPart2() {
        _testISOLocalTimeConverter(1458995247100L, "HH:mm:ss.S");
    }

    /**
     * Prints java.util.Date in the server's local timezone
     */
    private void _testISOLocalTimeConverter(long millis, String expectedPattern) {
        LocalTime time = fromMillis(millis);
        String expected = DateTimeFormatter.ofPattern(expectedPattern).format(time);
        assertEquals(expected, converter.asString(time));
    }

    private static LocalTime fromMillis(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()).toLocalTime();
    }
}
