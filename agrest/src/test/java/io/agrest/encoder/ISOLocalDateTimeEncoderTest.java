package io.agrest.encoder;

import org.junit.Test;

import java.time.LocalDateTime;

import static io.agrest.encoder.Encoders.toJson;
import static org.junit.Assert.assertEquals;

public class ISOLocalDateTimeEncoderTest {

    private static int millisecond = 1_000_000; // millisecond is 10^6 nanoseconds

    private Encoder encoder = ISOLocalDateTimeEncoder.encoder();

    @Test
    public void testISOLocalDateTimeEncoder() {
        assertEquals("\"2016-03-26T15:27:27\"", toJson(encoder, LocalDateTime.of(2016, 3, 26, 15, 27, 27)));
    }

    @Test
    public void testISOLocalDateTimeEncoder_FractionalPart1() {
        assertEquals("\"2016-03-26T15:27:27.001\"", toJson(encoder, LocalDateTime.of(2016, 3, 26, 15, 27, 27, millisecond)));
    }

    @Test
    public void testISOLocalDateTimeEncoder_FractionalPart2() {
        assertEquals("\"2016-03-26T15:27:27.1\"", toJson(encoder, LocalDateTime.of(2016, 3, 26, 15, 27, 27, 100 * millisecond)));
    }

    @Test
    public void testISOLocalDateTimeEncoder_FractionalPart3() {
        // fractional part is not printed, when less than a millisecond
        assertEquals("\"2016-03-26T15:27:27\"", toJson(encoder, LocalDateTime.of(2016, 3, 26, 15, 27, 27, 1)));
        assertEquals("\"2016-03-26T15:27:27\"", toJson(encoder, LocalDateTime.of(2016, 3, 26, 15, 27, 27, 999_999)));
    }
}
