package com.nhl.link.rest.encoder;

import com.nhl.link.rest.encoder.converter.ISOLocalDateConverter;
import com.nhl.link.rest.encoder.converter.StringConverter;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class ISOLocalDateConverterTest {

    private StringConverter converter = ISOLocalDateConverter.converter();

    @Test
    public void testISOLocalDateConverter() {
        assertEquals("2016-03-26", converter.asString(LocalDate.of(2016, 3, 26)));
    }
}
