package io.agrest.jaxrs2.openapi;

import io.agrest.DataResponse;
import io.agrest.annotation.AgRelationship;
import io.agrest.jaxrs2.openapi.junit.TestOpenAPIBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Entity_RelationshipsTest {

    static final OpenAPI oapi = new TestOpenAPIBuilder()
            .addPackage(P5.class)
            .addClass(Resource.class)
            .build();

    @Test
    public void testToOne() {
        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P5").getProperties();
        Schema schema = props.get("p6");

        assertNull(schema.getName());
        assertNull(schema.getType());
        assertNull(schema.getFormat());
        assertEquals("#/components/schemas/P6", schema.get$ref());
    }

    @Test
    public void testToMany() {
        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P6").getProperties();
        Schema schema = props.get("p5s");

        assertNull(schema.getName());
        assertEquals("array", schema.getType());

        assertTrue(schema instanceof ArraySchema);

        Schema itemSchema = ((ArraySchema) schema).getItems();
        assertNull(itemSchema.getName());
        assertNull(itemSchema.getType());
        assertNull(itemSchema.getFormat());
        assertEquals("#/components/schemas/P5", itemSchema.get$ref());
    }

    @Path("r")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("p5")
        public DataResponse<P5> getP5() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @GET
        @Path("p6")
        public DataResponse<P6> getP6() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }
    }

    public static class P5 {

        @AgRelationship
        public P6 getP6() {
            return null;
        }
    }

    public static class P6 {

        @AgRelationship
        public List<P5> getP5s() {
            return null;
        }
    }
}
