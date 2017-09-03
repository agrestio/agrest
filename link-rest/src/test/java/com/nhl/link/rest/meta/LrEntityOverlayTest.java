package com.nhl.link.rest.meta;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LrEntityOverlayTest {

    @Test
    public void testAddAttribute() {

        LrEntityOverlay<T1> overlay = new LrEntityOverlay<>(T1.class)
                .addAttribute("u")
                .addAttribute("x")
                .addAttribute("y")
                .addAttribute("z");

        Map<String, LrAttribute> attributes = new HashMap<>();
        overlay.getAttributes().forEach(a -> attributes.put(a.getName(), a));

        assertEquals(int.class, attributes.get("u").getType());
        assertEquals(String.class, attributes.get("x").getType());
        assertEquals(boolean.class, attributes.get("y").getType());
        assertEquals(String.class, attributes.get("z").getType());

    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttribute_Missing() {
        new LrEntityOverlay<>(T1.class).addAttribute("a");
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
}
