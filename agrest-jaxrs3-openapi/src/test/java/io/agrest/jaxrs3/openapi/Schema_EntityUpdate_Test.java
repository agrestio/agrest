package io.agrest.jaxrs3.openapi;

import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.jaxrs3.openapi.junit.TestOpenAPIBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class Schema_EntityUpdate_Test {

    static final OpenAPI oapi = new TestOpenAPIBuilder()
            .addPackage(P4.class)
            .addClass(Resource.class)
            .build();

    @Test
    public void testIdsAndAttributes() {
        Schema p4 = oapi.getComponents().getSchemas().get("EntityUpdate(P4)");
        assertNotNull(p4);

        Map<String, Schema> props = p4.getProperties();
        assertEquals(new HashSet(asList("b", "c", "id")), props.keySet());

        Schema id = props.get("id");
        assertNull(id.getName());
        assertEquals("integer", id.getType());
        assertEquals("int32", id.getFormat());

        Schema b = props.get("b");
        assertNull(b.getName());
        assertEquals("integer", b.getType());
        assertEquals("int32", b.getFormat());

        Schema c = props.get("c");
        assertNull(c.getName());
        assertEquals("string", c.getType());
        assertNull(c.getFormat());
    }

    @Path("r")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("collection")
        public SimpleResponse putP4(List<EntityUpdate<P4>> p4Updates) {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }
    }

    public static class P4 {

        @AgId
        public int getA() {
            return -1;
        }

        @AgAttribute
        public int getB() {
            return -1;
        }

        @AgAttribute
        public String getC() {
            return "";
        }
    }
}
