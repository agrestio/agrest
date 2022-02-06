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

import static org.junit.jupiter.api.Assertions.*;

public class Schema_EntityRelationships_ReachabilityTest {

    static final OpenAPI oapi = new TestOpenAPIBuilder()
            .addPackage(P9.class)
            .addClass(Resource.class)
            .build();

    @Test
    public void testRelatedIncluded() {
        // P9, P10, P11 are all related to each other, some have circular relationships

        Schema p9 = oapi.getComponents().getSchemas().get("P9");
        Schema p10 = oapi.getComponents().getSchemas().get("P10");
        Schema p11 = oapi.getComponents().getSchemas().get("P11");

        assertNotNull(p9);
        assertNotNull(p10, "Reachable entity was not included");
        assertNotNull(p11, "Reachable entity was not included");

        Schema p10_fromP9 = (Schema) p9.getProperties().get("p10");
        assertNull(p10_fromP9.getName());
        assertNull(p10_fromP9.getType());
        assertNull(p10_fromP9.getFormat());
        assertEquals("#/components/schemas/P10", p10_fromP9.get$ref());

        Schema p9_fromP10Array = (Schema) p10.getProperties().get("p9s");
        assertNull(p9_fromP10Array.getName());
        assertEquals("array", p9_fromP10Array.getType());
        assertTrue(p9_fromP10Array instanceof ArraySchema);

        Schema p9_fromP10 = ((ArraySchema) p9_fromP10Array).getItems();
        assertNull(p9_fromP10.getName());
        assertNull(p9_fromP10.getType());
        assertNull(p9_fromP10.getFormat());
        assertEquals("#/components/schemas/P9", p9_fromP10.get$ref());
    }

    @Path("r")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("p9")
        public DataResponse<P9> getP9() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }
    }

    public static class P9 {

        @AgRelationship
        public P10 getP10() {
            throw new UnsupportedOperationException("the actual value is not relevant");
        }
    }

    public static class P10 {

        @AgRelationship
        public P10 getP10() {
            throw new UnsupportedOperationException("the actual value is not relevant");
        }

        @AgRelationship
        public List<P9> getP9s() {
            throw new UnsupportedOperationException("the actual value is not relevant");
        }

        @AgRelationship
        public P11 getP11() {
            throw new UnsupportedOperationException("the actual value is not relevant");
        }
    }

    public static class P11 {

        @AgRelationship
        public P9 getP9() {
            throw new UnsupportedOperationException("the actual value is not relevant");
        }
    }
}
