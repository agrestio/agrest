package io.agrest.base.jsonvalueconverter;

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ISOLocalTimeConverterTest {

    @Test
    public void testConvert() {
        Object time = ISOLocalTimeConverter.converter().value(new TextNode("16:58:47"));
        assertEquals(LocalTime.class, time.getClass());
        assertEquals(LocalTime.of(16, 58, 47), time);
    }
}
