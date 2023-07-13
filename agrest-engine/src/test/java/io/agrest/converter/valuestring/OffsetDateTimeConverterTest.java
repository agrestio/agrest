package io.agrest.converter.valuestring;

import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OffsetDateTimeConverterTest {

    private ValueStringConverter converter = OffsetDateTimeConverter.converter();

    @Test
    public void iSOOffsetDateTimeConverter() {
    	_testISOOffsetDateTimeConverter(1458995247000L);
    }

    @Test
    public void iSOOffsetDateTimeConverter_FractionalPart1() {
    	_testISOOffsetDateTimeConverter(1458995247001L);
    }

    @Test
    public void iSOOffsetDateTimeConverter_FractionalPart2() {
    	_testISOOffsetDateTimeConverter(1458995247100L);
    }

    private void _testISOOffsetDateTimeConverter(long millis) {
    	OffsetDateTime dateTime = fromMillis(millis);
        String expected = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
        assertEquals(expected, converter.asString(dateTime));
    }

    private static OffsetDateTime fromMillis(long millis) {
        return OffsetDateTime.of(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()), ZoneOffset.ofHours(3));
    }
}
