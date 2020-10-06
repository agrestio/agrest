package io.agrest.sencha.ops;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilterUtilTest {

    @Test
    public void testEscapeValueForLike() {
        assertEquals("", FilterUtil.escapeValueForLike(""));
        assertEquals("abc", FilterUtil.escapeValueForLike("abc"));

        assertEquals("\\%abc", FilterUtil.escapeValueForLike("%abc"));
        assertEquals("a\\%bc", FilterUtil.escapeValueForLike("a%bc"));
        assertEquals("abc\\%", FilterUtil.escapeValueForLike("abc%"));

        assertEquals("\\_abc", FilterUtil.escapeValueForLike("_abc"));
        assertEquals("a\\_bc", FilterUtil.escapeValueForLike("a_bc"));
        assertEquals("abc\\_", FilterUtil.escapeValueForLike("abc_"));

        assertEquals("a\\_b\\%c", FilterUtil.escapeValueForLike("a_b%c"));
        assertEquals("abc\\%\\%", FilterUtil.escapeValueForLike("abc%%"));
        assertEquals("\\%\\%\\_\\_abc", FilterUtil.escapeValueForLike("%%__abc"));
    }
}
