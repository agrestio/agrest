package io.agrest.meta;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AgEntityOverlayTest {

    @Deprecated
    @Test
    public void testAddAttribute() {

        AgEntityOverlay<T1> overlay = new AgEntityOverlay<>(T1.class)
                .addAttribute("u")
                .addAttribute("x")
                .addAttribute("y");

        Map<String, AgAttribute> attributes = new HashMap<>();
        overlay.getAttributes().forEach(a -> attributes.put(a.getName(), a));

        assertEquals(int.class, attributes.get("u").getType());
        assertEquals(String.class, attributes.get("x").getType());
        assertEquals(boolean.class, attributes.get("y").getType());
    }

    @Deprecated
    @Test
    public void testAddAttribute_Inheritance() {

        AgEntityOverlay<T2> overlay = new AgEntityOverlay<>(T2.class)
                .addAttribute("u")
                .addAttribute("x")
                .addAttribute("y")
                .addAttribute("a");

        Map<String, AgAttribute> attributes = new HashMap<>();
        overlay.getAttributes().forEach(a -> attributes.put(a.getName(), a));

        assertEquals(int.class, attributes.get("u").getType());
        assertEquals(String.class, attributes.get("x").getType());
        assertEquals(boolean.class, attributes.get("y").getType());
        assertEquals(int.class, attributes.get("a").getType());

    }

    @Deprecated
    @Test
    public void testAddAttribute_Missing() {
        assertThrows(IllegalArgumentException.class, () -> new AgEntityOverlay<>(T1.class).addAttribute("a"));
    }

    public static class T1 {

        public int getU() {
            return -1;
        }

        public String getX() {
            return "xx";
        }

        public boolean isY() {
            return false;
        }

        public void setZ(String z) {
        }
    }

    public static class T2 extends T1 {

        public int getA() {
            return -1;
        }
    }
}
