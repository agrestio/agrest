package io.agrest.converter.valuejson;

import io.agrest.encoder.Encoder;
import io.agrest.encoder.ISODateEncoder;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static io.agrest.runtime.encoder.Encoders.toJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ISODateEncoderTest {

    private Encoder encoder = ISODateEncoder.encoder();

    @Test
    public void testISODateEncoder() {
        assertEquals("\"2016-03-26\"", toJson(encoder, new Date(1458995247000L)));
    }
}
