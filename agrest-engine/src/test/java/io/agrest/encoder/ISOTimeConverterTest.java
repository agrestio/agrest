package io.agrest.encoder;

import io.agrest.encoder.converter.ISOTimeConverter;
import io.agrest.encoder.converter.StringConverter;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ISOTimeConverterTest {

    private StringConverter converter = ISOTimeConverter.converter();

    @Test
    public void testISOTimeConverter() {
        _testISOTimeConverter(1458995247000L, "HH:mm:ss");
    }

    @Test
    public void testISOTimeConverter_FractionalPart1() {
        _testISOTimeConverter(1458995247001L, "HH:mm:ss.SSS");
    }

    @Test
    public void testISOTimeConverter_FractionalPart2() {
        _testISOTimeConverter(1458995247100L, "HH:mm:ss.S");
    }

    /**
     * Prints java.util.Date in the server's local timezone
     */
    private void _testISOTimeConverter(long millis, String expectedPattern) {
        String expected = DateTimeFormatter.ofPattern(expectedPattern).format(fromMillis(millis));
        assertEquals(expected, converter.asString(new Date(millis)));
    }

    private static LocalTime fromMillis(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()).toLocalTime();
    }
}
