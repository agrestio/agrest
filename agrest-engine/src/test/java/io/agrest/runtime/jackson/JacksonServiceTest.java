package io.agrest.runtime.jackson;

import io.agrest.AgException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JacksonServiceTest {

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
}
