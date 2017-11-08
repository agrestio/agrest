package com.nhl.link.rest.encoder.legacy;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class LegacyDateConvertersTest {

    private static final long millis = 1458995247000L;

    @Test
    public void testLegacyDateConverters_isoDate() {
        assertEquals("2016-03-26", ISODateConverter.converter().asString(new java.util.Date(millis)));
    }

    @Test
    public void testLegacyDateConverters_isoDateTime() {
        assertEquals("2016-03-26T12:27:27Z", ISODateTimeConverter.converter().asString(new java.sql.Date(millis)));
    }

    @Test
    public void testLegacyDateConverters_isoTime() {
        LocalTime localTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()).toLocalTime();
        String expected = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault()).format(localTime);
        assertEquals(expected, ISOTimeConverter.converter().asString(new Date(millis)));
    }
}
