package com.nhl.link.rest.parser.converter;

import com.fasterxml.jackson.databind.node.TextNode;
import com.nhl.link.rest.encoder.DateTimeFormatters;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class UtcDateConverterTest {

    @Test
    public void testConverter_javaUtilDate() {
        java.util.Date date = new java.util.Date();
        java.util.Date parsed = convert(java.util.Date.class, isoFormat(date));
        assertEquals(date, parsed);
    }

    @Test
    public void testConverter_javaSqlDate() {
        java.util.Date date = new java.util.Date();
        java.sql.Date parsed = convert(java.sql.Date.class, isoFormat(date));
        assertEquals(date, parsed);
    }

    @Test
    public void testConverter_javaSqlTime() {
        java.util.Date date = new java.util.Date();
        java.sql.Time parsed = convert(java.sql.Time.class, isoFormat(date));
        assertEquals(date, parsed);
    }

    @Test
    public void testConverter_javaSqlTimestamp() {
        java.util.Date date = new java.util.Date();
        java.sql.Timestamp parsed = convert(java.sql.Timestamp.class, isoFormat(date));
        assertEquals(date, parsed);
    }

    private static String isoFormat(java.util.Date date) {
        return DateTimeFormatters.isoLocalDateTime().format(Instant.ofEpochMilli(date.getTime()));
    }

    @SuppressWarnings("unchecked")
    private static <T extends java.util.Date> T convert(Class<T> targetType, String value) {
        return (T) UtcDateConverter.converter(targetType).value(new TextNode(value));
    }
}
