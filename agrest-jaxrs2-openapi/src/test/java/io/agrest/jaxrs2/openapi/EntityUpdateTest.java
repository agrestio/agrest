package io.agrest.jaxrs2.openapi;

import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.jaxrs2.openapi.junit.TestOpenAPIBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EntityUpdateTest {

    static final OpenAPI oapi = new TestOpenAPIBuilder()
            .addPackage(P4.class)
            .addClass(Resource.class)
            .build();

    @Test
    public void testPropertiesPresentAndOrdered() {
        Schema p4Update = oapi.getComponents().getSchemas().get("EntityUpdate(P4)");
        assertNotNull(p4Update);

        Map<String, Schema> props = p4Update.getProperties();

        // ordering matters and must be alphabetic within property groups
        assertEquals(List.of("id", "b", "p5", "p6s", "z"), List.copyOf(props.keySet()));

        Schema id = props.get("id");
        assertNotNull(id);
        assertEquals("integer", id.getType());
        assertEquals("int32", id.getFormat());

        Schema b = props.get("b");
        assertNotNull(b);
        assertEquals("integer", b.getType());
        assertEquals("int32", b.getFormat());

        Schema z = props.get("z");
        assertNotNull(z);
        assertEquals("string", z.getType());

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
    public void testProperties_FilterByAccess() {
        Schema p8Update = oapi.getComponents().getSchemas().get("EntityUpdate(P8)");
        assertNotNull(p8Update);

        Map<String, Schema> props = p8Update.getProperties();

        assertEquals(List.of("b"), List.copyOf(props.keySet()));

        Schema b = props.get("b");
        assertNotNull(b);
        assertEquals("integer", b.getType());
        assertEquals("int32", b.getFormat());
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
        @Path("p4")
        public SimpleResponse putP4(List<EntityUpdate<P4>> u) {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @PUT
        @Path("p7")
        public SimpleResponse putP7(@PathParam("id") int id, EntityUpdate<P7> u) {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @PUT
        @Path("p8")
        public SimpleResponse putP8(List<EntityUpdate<P8>> u) {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }
    }

    public static class P4 {

        // intentionally declare properties in non-alphabetic order

        @AgAttribute
        public String getZ() {
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

    public static class P8 {

        @AgAttribute
        public int getB() {
            return -1;
        }

        @AgAttribute(writable = false)
        public String getZ() {
            return "";
        }

        @AgId(writable = false)
        public int getA() {
            return -1;
        }

        @AgRelationship(writable = false)
        public Set<P6> getP6s() {
            return Set.of();
        }

        @AgRelationship(writable = false)
        public P5 getP5() {
            return null;
        }
    }
}
