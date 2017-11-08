package com.nhl.link.rest.encoder;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.nhl.link.rest.encoder.Encoders.jsonString;
import static com.nhl.link.rest.encoder.Encoders.toJson;
import static org.junit.Assert.assertEquals;

public class ISODateTimeEncoderTest {

    private Encoder encoder = ISODateTimeEncoder.encoder();

    @Test
    public void testISODateTimeEncoder() {
        _testISODateTimeEncoder(1458995247000L, "yyyy-MM-dd'T'HH:mm:ss");
    }

    @Test
    public void testISODateTimeEncoder_FractionalPart1() {
        _testISODateTimeEncoder(1458995247001L, "yyyy-MM-dd'T'HH:mm:ss.SSS");
    }

    @Test
    public void testISODateTimeEncoder_FractionalPart2() {
        _testISODateTimeEncoder(1458995247100L, "yyyy-MM-dd'T'HH:mm:ss.S");
    }

    /**
     * Prints java.util.Date in the server's local timezone
     */
    private void _testISODateTimeEncoder(long millis, String expectedPattern) {
        String expected = DateTimeFormatter.ofPattern(expectedPattern).format(fromMillis(millis));
        assertEquals(jsonString(expected), toJson(encoder, new Date(millis)));
    }

    private static LocalDateTime fromMillis(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }
}
