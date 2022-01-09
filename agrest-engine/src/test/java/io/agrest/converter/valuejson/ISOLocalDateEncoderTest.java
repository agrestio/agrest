package io.agrest.converter.valuejson;

import io.agrest.encoder.Encoder;
import io.agrest.encoder.ISOLocalDateEncoder;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.agrest.runtime.encoder.Encoders.toJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ISOLocalDateEncoderTest {

    private Encoder encoder = ISOLocalDateEncoder.encoder();

    @Test
    public void testISOLocalDateEncoder() {
        assertEquals("\"2016-03-26\"", toJson(encoder, LocalDate.of(2016, 3, 26)));
    }
}
