package io.agrest.openapi;

import io.agrest.DataResponse;
import io.agrest.openapi.unit.OpenAPIBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.example.entity.P1;
import org.example.entity.P3;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SchemaTest {

    static final OpenAPI oapi = new OpenAPIBuilder()
            .addPackage(P1.class)
            .addClass(Resource.class)
            .build();

    @Test
    public void testIdAndAttributes() {
        Schema p3 = oapi.getComponents().getSchemas().get("P3");
        assertNotNull(p3);
        assertEquals(new HashSet(asList("a", "b", "c", "d")), p3.getProperties().keySet());
    }

    @Path("r")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        public DataResponse<P3> getP3() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }
    }
}
