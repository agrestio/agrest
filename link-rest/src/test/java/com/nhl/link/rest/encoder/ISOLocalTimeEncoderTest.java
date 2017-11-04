package com.nhl.link.rest.encoder;

import org.junit.Test;

import java.time.LocalTime;

import static com.nhl.link.rest.encoder.Encoders.toJson;
import static org.junit.Assert.assertEquals;

public class ISOLocalTimeEncoderTest {

    private static int millisecond = 1_000_000; // millisecond is 10^6 nanoseconds

    private Encoder encoder = ISOLocalTimeEncoder.encoder();

    @Test
    public void testISOLocalTimeEncoder() {
        assertEquals("\"15:27:27\"", toJson(encoder, LocalTime.of(15, 27, 27)));
    }

    @Test
    public void testISOLocalTimeEncoder_FractionalPart1() {
        assertEquals("\"15:27:27.001\"", toJson(encoder, LocalTime.of(15, 27, 27, millisecond)));
    }

    @Test
    public void testISOLocalTimeEncoder_FractionalPart2() {
        assertEquals("\"15:27:27.1\"", toJson(encoder, LocalTime.of(15, 27, 27, 100 * millisecond)));
    }

    @Test
    public void testISOLocalTimeEncoder_FractionalPart3() {
        // fractional part is not printed, when less than a millisecond
        assertEquals("\"15:27:27\"", toJson(encoder, LocalTime.of(15, 27, 27, 1)));
        assertEquals("\"15:27:27\"", toJson(encoder, LocalTime.of(15, 27, 27, 999_999)));
    }
}
