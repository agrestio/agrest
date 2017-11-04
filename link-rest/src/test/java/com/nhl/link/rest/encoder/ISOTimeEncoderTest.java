package com.nhl.link.rest.encoder;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.nhl.link.rest.encoder.Encoders.jsonString;
import static com.nhl.link.rest.encoder.Encoders.toJson;
import static org.junit.Assert.assertEquals;

public class ISOTimeEncoderTest {

    private Encoder encoder = ISOTimeEncoder.encoder();

    @Test
    public void testISOTimeEncoder() {
        assertEquals("\"15:27:27\"", toJson(encoder, new Date(1458995247000L)));
    }

    @Test
    public void testISOTimeEncoder_FractionalPart1() {
        // expecting 15:27:27.001
        _testISOTimeEncoder_FractionaPart(1458995247001L, "HH:mm:ss.SSS");
    }

    @Test
    public void testISOTimeEncoder_FractionalPart2() {
        // expecting 15:27:27.1
        _testISOTimeEncoder_FractionaPart(1458995247100L, "HH:mm:ss.S");
    }

    private void _testISOTimeEncoder_FractionaPart(long millis, String expectedPattern) {
        String expected = DateTimeFormatter.ofPattern(expectedPattern).format(fromMillis(millis));
        assertEquals(jsonString(expected), toJson(encoder, new Date(millis)));
    }

    private static LocalTime fromMillis(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()).toLocalTime();
    }
}
