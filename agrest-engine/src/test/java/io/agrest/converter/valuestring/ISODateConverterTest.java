package io.agrest.converter.valuestring;

import io.agrest.converter.valuestring.ISODateConverter;
import io.agrest.converter.valuestring.ValueStringConverter;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ISODateConverterTest {

    private ValueStringConverter converter = ISODateConverter.converter();

    @Test
    public void testISODateConverter() {
        assertEquals("2016-03-26", converter.asString(new Date(1458995247000L)));
    }
}
