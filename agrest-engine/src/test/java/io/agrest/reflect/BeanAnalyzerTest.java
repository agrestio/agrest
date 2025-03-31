package io.agrest.reflect;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BeanAnalyzerTest {

    @Test
    public void findGetters() {
        Map<String, PropertyGetter> getters = new HashMap<>();
        BeanAnalyzer.findGetters(C1.class).forEach(g -> getters.put(g.getName(), g));
        assertEquals("abC,get,get1,getabc,is,is1,isMe,xyZ",
                getters.keySet().stream().sorted().collect(Collectors.joining(",")));

        assertEquals(Boolean.class, getters.get("isMe").getType());
        assertEquals(Boolean.TYPE, getters.get("xyZ").getType());
    }

    @Test
    public void findGetters_Inheritance() {
        Map<String, PropertyGetter> getters = new HashMap<>();
        BeanAnalyzer.findGetters(C2.class).forEach(g -> getters.put(g.getName(), g));
        assertEquals("abC,get,get1,getabc,is,is1,isMe,me,xyZ",
                getters.keySet().stream().sorted().collect(Collectors.joining(",")));
    }

    @Test
    public void findSetters() {
        Map<String, PropertySetter> setters = new HashMap<>();
        BeanAnalyzer.findSetters(C1.class).forEach(s -> setters.put(s.getName(), s));
        assertEquals("x,yZ",
                setters.keySet().stream().sorted().collect(Collectors.joining(",")));
    }

    @Test
    public void findSetters_Inheritance() {
        Map<String, PropertySetter> setters = new HashMap<>();
        BeanAnalyzer.findSetters(C2.class).forEach(s -> setters.put(s.getName(), s));
        assertEquals("x,yZ,yza",
                setters.keySet().stream().sorted().collect(Collectors.joining(",")));
    }

    static class C1 {

        // getters

        // non-public, must exclude
        protected int getXyz() {
            return 0;
        }

        public int get() {
            return 0;
        }

        public int get1() {
            return 0;
        }

        public int getabc() {
            return 0;
        }

        public int getAbC() {
            return 0;
        }

        // void, must exclude
        public void get2() {
        }

        public Boolean isMe() {
            return null;
        }

        public boolean is() {
            return false;
        }

        public boolean is1() {
            return false;
        }

        public boolean isXyZ() {
            return false;
        }

        // setters

        // non-public, must exclude
        protected void setMe(String me) {
        }

        // not a prop, must exclude
        public void set(String me) {
        }

        // not a prop, must exclude
        public void set1(String me) {
        }

        public void setX(String me) {
        }

        public void setYZ(boolean me) {
        }
    }

    static class C2 extends C1 {

        @Override
        public int getAbC() {
            return 0;
        }

        public int getMe() {
            return 0;
        }

        // setters

        @Override
        public void setYZ(boolean me) {
        }

        public void setYza(boolean me) {
        }
    }
}
