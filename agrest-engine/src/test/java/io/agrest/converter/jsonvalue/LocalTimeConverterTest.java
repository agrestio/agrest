package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalTimeConverterTest {

    @Test
    public void testConvert() {
        Object time = LocalTimeConverter.converter().value(new TextNode("16:58:47"));
        assertEquals(LocalTime.class, time.getClass());
        assertEquals(LocalTime.of(16, 58, 47), time);
    }
}
