package io.agrest.jaxrs3.openapi;

import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.jaxrs3.openapi.junit.TestOpenAPIBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EntityUpdateTest {

    static final OpenAPI oapi = new TestOpenAPIBuilder()
            .addPackage(P4.class)
            .addClass(Resource.class)
            .build();

    @Test
    public void testProperties() {
        Schema p4Update = oapi.getComponents().getSchemas().get("EntityUpdate(P4)");
        assertNotNull(p4Update);

        Map<String, Schema> props = p4Update.getProperties();

        // ordering matters and must be alphabetic within property groups
        assertEquals(List.of("id", "b", "c", "p5", "p6s"), List.copyOf(props.keySet()));

        assertNotNull(p4Update);

        Schema id = props.get("id");
        assertNotNull(id);
        assertEquals("integer", id.getType());
        assertEquals("int32", id.getFormat());

        Schema b = props.get("b");
        assertNotNull(b);
        assertEquals("integer", b.getType());
        assertEquals("int32", b.getFormat());

        Schema c = props.get("c");
        assertNotNull(c);
        assertEquals("string", c.getType());

        Schema p5 = props.get("p5");
        assertNotNull(p5);
        assertEquals("integer", p5.getType());
        assertEquals("int64", p5.getFormat());

        Schema p6s = props.get("p6s");
        assertNotNull(p6s);
        assertEquals("array", p6s.getType());

        Schema p6sItems = ((ArraySchema) p6s).getItems();
        assertNotNull(p6sItems);
        assertEquals("string", p6sItems.getType());
    }

    @Test
    public void testProperties_MultiColumnId() {

        Schema p7Update = oapi.getComponents().getSchemas().get("EntityUpdate(P7)");
        assertNotNull(p7Update);

        assertEquals(1, p7Update.getProperties().size());
        Schema idSchema = (Schema) p7Update.getProperties().get("id");
        assertNotNull(idSchema);

        assertEquals(2, idSchema.getProperties().size());

        Schema id1Schema = (Schema) idSchema.getProperties().get("id1");
        assertNotNull(id1Schema);
        assertEquals("integer", id1Schema.getType());
        assertEquals("int32", id1Schema.getFormat());

        Schema id2Schema = (Schema) idSchema.getProperties().get("id2");
        assertNotNull(id2Schema);
        assertEquals("string", id2Schema.getType());
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

        @PUT
        @Path("single/multi-id/{id}")
        public SimpleResponse putP7ById(@PathParam("id") int id, EntityUpdate<P7> update) {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }
    }

    public static class P4 {

        // intentionally declare properties in non-alphabetic order

        @AgAttribute
        public String getC() {
            return "";
        }

        @AgAttribute
        public int getB() {
            return -1;
        }

        @AgId
        public int getA() {
            return -1;
        }

        @AgRelationship
        public Set<P6> getP6s() {
            return Set.of();
        }

        @AgRelationship
        public P5 getP5() {
            return null;
        }
    }

    public static class P5 {
        @AgId
        public long getA() {
            return -1L;
        }

        @AgAttribute
        public int getB() {
            return -1;
        }
    }

    public static class P6 {
        @AgId
        public String getA() {
            return "";
        }

        @AgAttribute
        public int getB() {
            return -1;
        }
    }

    public static class P7 {
        @AgId
        public int getId1() {
            return -1;
        }

        @AgId
        public String getId2() {
            return "";
        }
    }
}
