package io.agrest.openapi;

import io.agrest.DataResponse;
import io.agrest.openapi.unit.OpenAPIBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import org.example.entity.P1;
import org.example.entity.P5;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Schema_EntityRelationships_ReachabilityTest {

    static final OpenAPI oapi = new OpenAPIBuilder()
            .addPackage(P1.class)
            .addClass(Resource.class)
            .build();

    @Test
    @Disabled("work in progress")
    public void testRelatedIncluded() {
        assertNotNull(oapi.getComponents().getSchemas().get("P5"));
        assertNotNull(oapi.getComponents().getSchemas().get("P6"), "Reachable entity was not included");
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
    }
}
