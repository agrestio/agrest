package io.agrest.encoder;

import io.agrest.encoder.converter.ISOLocalDateConverter;
import io.agrest.encoder.converter.StringConverter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ISOLocalDateConverterTest {

    private StringConverter converter = ISOLocalDateConverter.converter();

    @Test
    public void testISOLocalDateConverter() {
        assertEquals("2016-03-26", converter.asString(LocalDate.of(2016, 3, 26)));
    }
}
