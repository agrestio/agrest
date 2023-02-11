package io.agrest.jaxrs3.openapi;

import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.jaxrs3.openapi.junit.TestOpenAPIBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EntityUpdateTest {

    static final OpenAPI oapi = new TestOpenAPIBuilder()
            .addClass(Resource.class)
            .addPackage(P4.class)
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

        Schema mtSchema = mt.getSchema();
        assertNotNull(mtSchema);

        assertNull(mtSchema.getName());
        assertNull(mtSchema.getType());
        assertEquals("#/components/schemas/EntityUpdate(P4)", mtSchema.get$ref());

        Schema updateSchema = oapi.getComponents().getSchemas().get("EntityUpdate(P4)");
        assertNotNull(updateSchema);

        assertEquals(5, updateSchema.getProperties().size());
        Schema idSchema = (Schema) updateSchema.getProperties().get("id");
        assertNotNull(idSchema);
        assertEquals("integer", idSchema.getType());
        assertEquals("int32", idSchema.getFormat());

        Schema bSchema = (Schema) updateSchema.getProperties().get("b");
        assertNotNull(bSchema);
        assertEquals("integer", bSchema.getType());
        assertEquals("int32", bSchema.getFormat());

        Schema cSchema = (Schema) updateSchema.getProperties().get("c");
        assertNotNull(cSchema);
        assertEquals("string", cSchema.getType());

        Schema p5Schema = (Schema) updateSchema.getProperties().get("p5");
        assertNotNull(p5Schema);
        assertEquals("integer", p5Schema.getType());
        assertEquals("int64", p5Schema.getFormat());

        Schema p6sSchema = (Schema) updateSchema.getProperties().get("p6s");
        assertNotNull(p6sSchema);
        assertEquals("array", p6sSchema.getType());

        Schema p6sItemsSchema = ((ArraySchema) p6sSchema).getItems();
        assertNotNull(p6sItemsSchema);
        assertEquals("string", p6sItemsSchema.getType());
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

        @AgRelationship
        public P5 getP5() {
            return null;
        }

        @AgRelationship
        public Set<P6> getP6s() {
            return Set.of();
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
}
