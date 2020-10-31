package io.agrest.openapi;

import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.openapi.unit.OpenAPIBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.example.entity.P1;
import org.example.entity.P4;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EntityUpdateTest {

    static final OpenAPI oapi = new OpenAPIBuilder()
            .addClass(Resource.class)
            .addPackage(P1.class)
            .build();

    @ParameterizedTest
    @ValueSource(strings = {"/r/single-untyped/{id}", "/r/single-wildcard/{id}"})
    public void testSingleUntyped(String uri) {
        PathItem pi = oapi.getPaths().get(uri);
        RequestBody rb = pi.getPut().getRequestBody();
        assertNotNull(rb);

        MediaType mt = rb.getContent().get("*/*");
        assertNotNull(mt);

        Schema schema = mt.getSchema();
        assertNotNull(schema);

        assertNull(schema.getName());
        assertNull(schema.getType());
        assertEquals("#/components/schemas/EntityUpdate(Object)", schema.get$ref());
    }

    @Test
    public void testSingleUpdate() {
        PathItem pi = oapi.getPaths().get("/r/single/{id}");
        RequestBody rb = pi.getPut().getRequestBody();
        assertNotNull(rb);

        MediaType mt = rb.getContent().get("*/*");
        assertNotNull(mt);

        Schema schema = mt.getSchema();
        assertNotNull(schema);

        assertNull(schema.getName());
        assertNull(schema.getType());
        assertEquals("#/components/schemas/EntityUpdate(P4)", schema.get$ref());
    }

    @Test
    public void testUpdateCollection() {
        PathItem pi = oapi.getPaths().get("/r/collection");
        RequestBody rb = pi.getPut().getRequestBody();
        assertNotNull(rb);

        MediaType mt = rb.getContent().get("*/*");
        assertNotNull(mt);

        Schema schema = mt.getSchema();
        assertNotNull(schema);

        assertNull(schema.getName());
        assertEquals("array", schema.getType());

        assertTrue(schema instanceof ArraySchema);

        Schema itemSchema = ((ArraySchema) schema).getItems();
        assertNull(itemSchema.getName());
        assertNull(itemSchema.getType());
        assertNull(itemSchema.getFormat());
        assertEquals("#/components/schemas/EntityUpdate(P4)", itemSchema.get$ref());
    }

    @Path("r")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("single-untyped/{id}")
        public SimpleResponse putByIdUntyped(@PathParam("id") int id, EntityUpdate update) {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @PUT
        @Path("single-wildcard/{id}")
        public SimpleResponse putByIdWildcard(@PathParam("id") int id, EntityUpdate<?> update) {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @PUT
        @Path("single/{id}")
        public SimpleResponse putP4ById(@PathParam("id") int id, EntityUpdate<P4> p4Update) {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @PUT
        @Path("collection")
        public SimpleResponse putP4(List<EntityUpdate<P4>> p4Updates) {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @PUT
        @Path("string/{id}")
        public SimpleResponse putP4ByIdString(@PathParam("id") int id, String data) {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @PUT
        @Path("string")
        public SimpleResponse putP4String(String data) {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }
    }
}
