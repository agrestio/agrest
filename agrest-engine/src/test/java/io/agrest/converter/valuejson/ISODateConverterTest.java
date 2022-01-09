package io.agrest.converter.valuejson;

import io.agrest.converter.valuejson.ISODateConverter;
import io.agrest.converter.valuejson.ValueJsonConverter;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ISODateConverterTest {

    private ValueJsonConverter converter = ISODateConverter.converter();

    @Test
    public void testISODateConverter() {
        assertEquals("2016-03-26", converter.asString(new Date(1458995247000L)));
    }
}
