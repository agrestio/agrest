package com.nhl.link.rest.runtime.parser.converter;

import com.fasterxml.jackson.databind.node.TextNode;
import com.nhl.link.rest.parser.converter.ISOLocalDateConverter;
import com.nhl.link.rest.parser.converter.ISOLocalDateTimeConverter;
import com.nhl.link.rest.parser.converter.ISOLocalTimeConverter;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.Assert.assertEquals;

public class Java8ISOConverterTest {

    private IJsonValueConverterFactory converterFactory;

    @Before
    public void before() {
        this.converterFactory = new Java8JsonValueConverterFactory();
    }

    @Test
    public void testJava8ISODate() {
        JsonValueConverter converter = this.converterFactory.converter(LocalDate.class.getName());
        assertEquals(ISOLocalDateConverter.class, converter.getClass());

        TextNode node = new TextNode("2016-03-26");
        Object date = converter.value(node);
        assertEquals(LocalDate.class, date.getClass());
        assertEquals(date, LocalDate.of(2016, 03, 26));
    }

    @Test
    public void testJava8ISOTime() {
        JsonValueConverter converter = this.converterFactory.converter(LocalTime.class.getName());
        assertEquals(ISOLocalTimeConverter.class, converter.getClass());

        TextNode node = new TextNode("16:58:47");
        Object time = converter.value(node);
        assertEquals(LocalTime.class, time.getClass());
        assertEquals(time, LocalTime.of(16, 58, 47));
    }

    @Test
    public void testJava8ISOTimestamp() {
        JsonValueConverter converter = this.converterFactory.converter(LocalDateTime.class.getName());
        assertEquals(ISOLocalDateTimeConverter.class, converter.getClass());

        TextNode node = new TextNode("2016-03-26T16:59:58");
        Object dateTime = converter.value(node);
        assertEquals(LocalDateTime.class, dateTime.getClass());
        assertEquals(dateTime, LocalDateTime.of(2016, 03, 26, 16, 59, 58));
    }
}
