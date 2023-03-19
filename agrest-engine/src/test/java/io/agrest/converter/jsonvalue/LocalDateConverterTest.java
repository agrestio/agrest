package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalDateConverterTest {

    @Test
    public void testJava8ISODate() {
        Object date = LocalDateConverter.converter().value(new TextNode("2016-03-26"));
        assertEquals(LocalDate.class, date.getClass());
        assertEquals(LocalDate.of(2016, 3, 26), date);
    }
}
