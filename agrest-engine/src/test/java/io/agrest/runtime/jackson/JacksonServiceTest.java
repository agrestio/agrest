package io.agrest.runtime.jackson;

import io.agrest.AgException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class JacksonServiceTest {

    @Test
    public void testOutputJson() throws IOException {
        JacksonService service = new JacksonService();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.outputJson(o -> o.writeObject(new X(1, "two")), out);
        assertEquals("{\"a\":1,\"b\":\"two\"}", out.toString());
    }

    @Test
    public void testParse_BadJson() {
        JacksonService service = new JacksonService();

        try {
            service.parseJson("{bad}");
            fail("Exception expected");
        } catch (AgException e) {

            assertEquals("Error parsing JSON", e.getMessage());
            assertEquals(400, e.getStatus());
            assertNotNull(e.getCause());
        }
    }

    public static class X {
        private final int a;
        private final String b;

        public X(int a, String b) {
            this.a = a;
            this.b = b;
        }

        public int getA() {
            return a;
        }

        public String getB() {
            return b;
        }
    }
}
