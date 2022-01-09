package io.agrest.converter.valuejson;

import io.agrest.encoder.Encoder;
import io.agrest.encoder.ISOOffsetDateTimeEncoder;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static io.agrest.runtime.encoder.Encoders.toJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ISOOffsetDateTimeEncoderTest {

    private static int millisecond = 1_000_000; // millisecond is 10^6 nanoseconds

    private Encoder encoder = ISOOffsetDateTimeEncoder.encoder();

    @Test
    public void testISOOffsetDateTimeEncoder() {
        assertEquals("\"2016-03-26T15:27:27+03:00\"", toJson(encoder, OffsetDateTime.of(LocalDateTime.of(2016, 3, 26, 15, 27, 27), ZoneOffset.ofHours(3))));
    }

    @Test
    public void testISOOffsetDateTimeEncoder_FractionalPart1() {
        assertEquals("\"2016-03-26T15:27:27.001+03:00\"", toJson(encoder, OffsetDateTime.of(LocalDateTime.of(2016, 3, 26, 15, 27, 27, millisecond), ZoneOffset.ofHours(3))));
    }

    @Test
    public void testISOOffsetDateTimeEncoder_FractionalPart2() {
        assertEquals("\"2016-03-26T15:27:27.1+03:00\"", toJson(encoder, OffsetDateTime.of(LocalDateTime.of(2016, 3, 26, 15, 27, 27, 100 * millisecond), ZoneOffset.ofHours(3))));
    }

    @Test
    public void testISOOffsetDateTimeEncoder_FractionalPart3() {
        // fractional part is not printed, when less than a millisecond
        assertEquals("\"2016-03-26T15:27:27.000000001+03:00\"", toJson(encoder, OffsetDateTime.of(LocalDateTime.of(2016, 3, 26, 15, 27, 27, 1), ZoneOffset.ofHours(3))));
        assertEquals("\"2016-03-26T15:27:27.000999999+03:00\"", toJson(encoder, OffsetDateTime.of(LocalDateTime.of(2016, 3, 26, 15, 27, 27, 999_999), ZoneOffset.ofHours(3))));
    }
}
