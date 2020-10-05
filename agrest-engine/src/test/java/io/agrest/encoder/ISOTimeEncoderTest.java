package io.agrest.encoder;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static io.agrest.encoder.Encoders.jsonString;
import static io.agrest.encoder.Encoders.toJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ISOTimeEncoderTest {

    private Encoder encoder = ISOTimeEncoder.encoder();

    @Test
    public void testISOTimeEncoder() {
        _testISOTimeEncoder(1458995247000L, "HH:mm:ss");
    }

    @Test
    public void testISOTimeEncoder_FractionalPart1() {
        _testISOTimeEncoder(1458995247001L, "HH:mm:ss.SSS");
    }

    @Test
    public void testISOTimeEncoder_FractionalPart2() {
        _testISOTimeEncoder(1458995247100L, "HH:mm:ss.S");
    }

    /**
     * Prints java.util.Date in the server's local timezone
     */
    private void _testISOTimeEncoder(long millis, String expectedPattern) {
        String expected = DateTimeFormatter.ofPattern(expectedPattern).format(fromMillis(millis));
        assertEquals(jsonString(expected), toJson(encoder, new Date(millis)));
    }

    private static LocalTime fromMillis(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()).toLocalTime();
    }
}
