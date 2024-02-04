package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.node.TextNode;
import io.agrest.AgException;
import io.agrest.converter.jsonvalue.EnumConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnumConverterTest {

    @Test
    public void convert() {
        EnumConverter c = new EnumConverter(E1.class);

        assertSame(E1.e11, c.value(new TextNode("e11")));
        assertSame(E1.e12, c.value(new TextNode("e12")));
        assertNull(c.value(new TextNode("")));
    }

    @Test
    public void convert_Invalid() {
        EnumConverter c = new EnumConverter(E1.class);
        assertThrows(AgException.class, () -> c.value(new TextNode("invalid")));
    }

    public enum E1 {
        e11, e12
    }
}
