package io.agrest.converter.valuestring;

import io.agrest.converter.valuestring.ISOLocalDateConverter;
import io.agrest.converter.valuestring.ValueStringConverter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ISOLocalDateConverterTest {

    private ValueStringConverter converter = ISOLocalDateConverter.converter();

    @Test
    public void testISOLocalDateConverter() {
        assertEquals("2016-03-26", converter.asString(LocalDate.of(2016, 3, 26)));
    }
}
