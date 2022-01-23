package io.agrest.openapi;

import io.agrest.DataResponse;
import io.agrest.annotation.AgAttribute;
import io.agrest.openapi.junit.TestOpenAPIBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ResponseSubresourceTest {

    static final OpenAPI oapi = new TestOpenAPIBuilder()
            .addClass(Resource.class)
            .addPackage(P1.class)
            .build();

    @Test
    public void testRootResource() {
        PathItem rootPi = oapi.getPaths().get("/r");
        assertNotNull(rootPi);
        Schema schema = rootPi.getGet().getResponses().getDefault().getContent().get("*/*").getSchema();
        assertEquals("#/components/schemas/DataResponse(P1)", schema.get$ref());
    }

    @Test
    public void testSubresource() {
        PathItem sub1Pi = oapi.getPaths().get("/r/subr/{id}");
        assertNotNull(sub1Pi);
        Schema sub1Schema = sub1Pi.getGet().getResponses().getDefault().getContent().get("*/*").getSchema();
        assertEquals("#/components/schemas/DataResponse(P2)", sub1Schema.get$ref());

        PathItem sub2Pi = oapi.getPaths().get("/r/subr/{id}/special");
        assertNotNull(sub2Pi);
        Schema sub2Schema = sub2Pi.getGet().getResponses().getDefault().getContent().get("*/*").getSchema();
        assertEquals("#/components/schemas/DataResponse(P2)", sub2Schema.get$ref());
    }

    @Path("r")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        public DataResponse<P1> getP1() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @Path("subr/{id}")
        public SubR getSubR(int id) {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }
    }

    public static class SubR {

        @GET
        public DataResponse<P2> getP2() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @GET
        @Path("special")
        public DataResponse<P2> getP2Special() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }
    }

    public static class P1 {

        @AgAttribute
        public int getA() {
            return -1;
        }
    }

    public static class P2 {

        @AgAttribute
        public int getA() {
            return -1;
        }
    }
}
