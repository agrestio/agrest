package io.agrest.openapi;

import io.agrest.DataResponse;
import io.agrest.openapi.unit.OpenAPIBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.example.entity.P1;
import org.example.entity.P5;
import org.example.entity.P6;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Schema_EntityRelationships_Test {

    static final OpenAPI oapi = new OpenAPIBuilder()
            .addPackage(P1.class)
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
}
