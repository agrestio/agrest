package io.agrest.openapi;

import io.agrest.DataResponse;
import io.agrest.annotation.AgRelationship;
import io.agrest.openapi.unit.OpenAPIBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Schema_EntityRelationships_ReachabilityTest {

    static final OpenAPI oapi = new OpenAPIBuilder()
            .addPackage(P9.class)
            .addClass(Resource.class)
            .build();

    @Test
    public void testRelatedIncluded() {
        // P9, P10, P11 are all related to each other, some have circular relationships
        assertNotNull(oapi.getComponents().getSchemas().get("P9"));
        assertNotNull(oapi.getComponents().getSchemas().get("P10"), "Reachable entity was not included");
        assertNotNull(oapi.getComponents().getSchemas().get("P11"), "Reachable entity was not included");
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
