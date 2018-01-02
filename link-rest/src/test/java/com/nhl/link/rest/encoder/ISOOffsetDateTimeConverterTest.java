package com.nhl.link.rest.encoder;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

import com.nhl.link.rest.encoder.converter.ISOOffsetDateTimeConverter;
import com.nhl.link.rest.encoder.converter.StringConverter;

public class ISOOffsetDateTimeConverterTest {

    private StringConverter converter = ISOOffsetDateTimeConverter.converter();

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
