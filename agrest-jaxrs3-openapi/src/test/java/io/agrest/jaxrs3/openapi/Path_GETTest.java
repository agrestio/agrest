package io.agrest.jaxrs3.openapi;

import io.agrest.DataResponse;
import io.agrest.entity.AgP1;
import io.agrest.jaxrs3.openapi.junit.TestOpenAPIBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import org.example.entity.NonAgP1;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class Path_GETTest {

    static final OpenAPI oapi = new TestOpenAPIBuilder()
            .addClass(Resource.class)
            .addPackage(NonAgP1.class)
            .addPackage(AgP1.class)
            .build();

    @Test
    public void dataResponseWithType() {
        PathItem pi = oapi.getPaths().get("/r/data-response-with-type");
        ApiResponses responses = pi.getGet().getResponses();

        assertEquals(1, responses.size());
        ApiResponse response = responses.getDefault();
        assertNotNull(response);

        assertEquals(1, response.getContent().size());
        MediaType mt = response.getContent().get("*/*");
        assertNotNull(mt);
        Schema schema = mt.getSchema();
        assertNotNull(schema);

        assertNull(schema.getName());
        assertNull(schema.getType());
        assertEquals("#/components/schemas/DataResponse(NonAgP1)", schema.get$ref());
    }

    @Test
    public void dataResponseWithIoAgrestType() {
        PathItem pi = oapi.getPaths().get("/r/data-response-with-ioagrest-type");
        ApiResponses responses = pi.getGet().getResponses();

        assertEquals(1, responses.size());
        ApiResponse response = responses.getDefault();
        assertNotNull(response);

        assertEquals(1, response.getContent().size());
        MediaType mt = response.getContent().get("*/*");
        assertNotNull(mt);
        Schema schema = mt.getSchema();
        assertNotNull(schema);

        assertNull(schema.getName());
        assertNull(schema.getType());
        assertEquals("#/components/schemas/DataResponse(AgP1)", schema.get$ref());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/r/data-response-bare", "/r/data-response-wildcard"})
    public void dataResponseWithNoType(String uri) {
        PathItem pi = oapi.getPaths().get(uri);
        ApiResponses responses = pi.getGet().getResponses();

        assertEquals(1, responses.size());
        ApiResponse response = responses.getDefault();
        assertNotNull(response);

        assertEquals(1, response.getContent().size());
        MediaType mt = response.getContent().get("*/*");
        assertNotNull(mt);
        Schema schema = mt.getSchema();
        assertNotNull(schema);

        assertNull(schema.getName());
        assertNull(schema.getType());
        assertEquals("#/components/schemas/DataResponse(Object)", schema.get$ref());
    }

    @Test
    public void responseAnnotated() {
        PathItem pi = oapi.getPaths().get("/r/response-annotated");
        ApiResponses responses = pi.getGet().getResponses();

        assertEquals(1, responses.size());
        ApiResponse response = responses.getDefault();
        assertNotNull(response);

        assertEquals(1, response.getContent().size());
        MediaType mt = response.getContent().get("*/*");
        assertNotNull(mt);
        Schema schema = mt.getSchema();
        assertNotNull(schema);

        assertNull(schema.getName());
        assertNull(schema.getType());
        assertEquals("#/components/schemas/DataResponse(Object)", schema.get$ref());
    }

    @Path("r")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("data-response-with-type")
        public DataResponse<NonAgP1> dataResponseWithType() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @GET
        @Path("data-response-with-ioagrest-type")
        public DataResponse<AgP1> dataResponseWithIOAgrestType() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @GET
        @Path("data-response-bare")
        public DataResponse dataResponseBare() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @GET
        @Path("data-response-wildcard")
        public DataResponse<?> dataResponseWildcard() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @GET
        @Path("response-annotated")
        @Operation(responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DataResponse.class))))
        public Response responseAnnotated() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }
    }
}
