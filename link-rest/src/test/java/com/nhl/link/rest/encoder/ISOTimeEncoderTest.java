package com.nhl.link.rest.encoder;

import org.junit.Test;

import java.util.Date;

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
        assertEquals("\"15:27:27.001\"", toJson(encoder, new Date(1458995247001L)));
    }

    @Test
    public void testISOTimeEncoder_FractionalPart2() {
        assertEquals("\"15:27:27.1\"", toJson(encoder, new Date(1458995247100L)));
    }
}
