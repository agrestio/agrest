package com.nhl.link.rest.encoder;

import org.junit.Test;

import java.time.LocalDate;

import static com.nhl.link.rest.encoder.Encoders.toJson;
import static org.junit.Assert.assertEquals;

public class ISOLocalDateEncoderTest {

    private Encoder encoder = ISOLocalDateEncoder.encoder();

    @Test
    public void testISOLocalDateEncoder() {
        assertEquals("\"2016-03-26\"", toJson(encoder, LocalDate.of(2016, 3, 26)));
    }
}
