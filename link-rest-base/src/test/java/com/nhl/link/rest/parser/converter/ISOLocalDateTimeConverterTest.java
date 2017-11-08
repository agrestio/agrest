package com.nhl.link.rest.parser.converter;

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class ISOLocalDateTimeConverterTest {

    @Test
    public void testJava8ISOTimestamp() {
        Object dateTime = ISOLocalDateTimeConverter.converter().value(new TextNode("2016-03-26T16:59:58"));
        assertEquals(LocalDateTime.class, dateTime.getClass());
        assertEquals(dateTime, LocalDateTime.of(2016, 03, 26, 16, 59, 58));
    }
}
