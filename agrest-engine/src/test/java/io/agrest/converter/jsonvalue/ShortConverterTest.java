package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.agrest.AgException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ShortConverterTest {

    @Test
    public void zero() {
        assertEquals(Short.valueOf((short) 0), convert((short) 0));
    }

    @Test
    public void integer_Zero() {
        assertEquals(Short.valueOf((short) 0), convert(0));
    }

    @Test
    public void minValue() {
        assertEquals(Short.MIN_VALUE, convert(Short.MIN_VALUE));
    }

    @Test
    public void maxValue() {
        assertEquals(Short.MAX_VALUE, convert(Short.MAX_VALUE));
    }

    @Test
    public void overflow() {
        assertThrows(AgException.class, () -> ShortConverter.converter().value(new IntNode(Short.MAX_VALUE + 5)));
    }

    private Short convert(Short value) {
        return ShortConverter.converter().value(new IntNode(value));
    }

    private Short convert(Integer value) {
        return ShortConverter.converter().value(new IntNode(value));
    }

    private Short convert(String value) {
        return ShortConverter.converter().value(new TextNode(value));
    }
}
