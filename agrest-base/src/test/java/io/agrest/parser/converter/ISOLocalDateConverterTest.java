package io.agrest.parser.converter;

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class ISOLocalDateConverterTest {

    @Test
    public void testJava8ISODate() {
        Object date = ISOLocalDateConverter.converter().value(new TextNode("2016-03-26"));
        assertEquals(LocalDate.class, date.getClass());
        assertEquals(LocalDate.of(2016, 03, 26), date);
    }
}
