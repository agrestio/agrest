package io.agrest.converter.valuejson;

import io.agrest.converter.valuejson.ISOOffsetDateTimeConverter;
import io.agrest.converter.valuejson.ValueJsonConverter;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ISOOffsetDateTimeConverterTest {

    private ValueJsonConverter converter = ISOOffsetDateTimeConverter.converter();

    @Test
    public void testISOOffsetDateTimeConverter() {
    	_testISOOffsetDateTimeConverter(1458995247000L);
    }

    @Test
    public void testISOOffsetDateTimeConverter_FractionalPart1() {
    	_testISOOffsetDateTimeConverter(1458995247001L);
    }

    @Test
    public void testISOOffsetDateTimeConverter_FractionalPart2() {
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
