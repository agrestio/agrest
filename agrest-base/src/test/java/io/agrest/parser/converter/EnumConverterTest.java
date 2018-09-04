package io.agrest.parser.converter;

import com.fasterxml.jackson.databind.node.TextNode;
import io.agrest.AgRESTException;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class EnumConverterTest {

    @Test
    public void testConvert() {
        EnumConverter c = new EnumConverter(E1.class);

        assertSame(E1.e11, c.value(new TextNode("e11")));
        assertSame(E1.e12, c.value(new TextNode("e12")));
        assertNull(c.value(new TextNode("")));
    }

    @Test(expected = AgRESTException.class)
    public void testConvert_Invalid() {
        EnumConverter c = new EnumConverter(E1.class);

        c.value(new TextNode("invalid"));
    }

    public enum E1 {
        e11, e12
    }
}
