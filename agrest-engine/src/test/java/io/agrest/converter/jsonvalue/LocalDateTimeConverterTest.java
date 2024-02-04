package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalDateTimeConverterTest {

    @Test
    public void java8ISOTimestamp() {
        Object dateTime = LocalDateTimeConverter.converter().value(new TextNode("2016-03-26T16:59:58"));
        assertEquals(LocalDateTime.class, dateTime.getClass());
        assertEquals(dateTime, LocalDateTime.of(2016, 3, 26, 16, 59, 58));
    }
}
