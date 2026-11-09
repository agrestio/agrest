package io.agrest.jaxrs.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.DataResponse;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.jaxrs.openapi.junit.TestOpenAPIBuilder;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.example.constraints.NonAgP2;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SerializationTest {

    static final OpenAPI oapi = new TestOpenAPIBuilder()
            .addPackage(P12.class)
            .addClass(Resource.class)
            .build();

    @Test
    public void json() throws JsonProcessingException {
        assertSpec(Json.mapper().readTree(Json.pretty().writeValueAsString(oapi)));
    }

    @Test
    public void yaml() throws JsonProcessingException {
        assertSpec(Yaml.mapper().readTree(Yaml.pretty().writeValueAsString(oapi)));
    }

    @Test
    public void beanValidationConstraints() {
        JsonNode props = Json.mapper().valueToTree(oapi)
                .path("components").path("schemas").path("NonAgP2").path("properties");

        assertEquals(2, props.path("sized").path("minLength").asInt());
        assertEquals(10, props.path("sized").path("maxLength").asInt());
        assertEquals(1, props.path("ranged").path("minimum").asInt());
        assertEquals(5, props.path("ranged").path("maximum").asInt());
        assertEquals("^[a-z]+$", props.path("patterned").path("pattern").asText());
    }

    private void assertSpec(JsonNode spec) {
        assertTrue(spec.path("openapi").asText().startsWith("3."), () -> "Unexpected version in: " + spec);

        JsonNode responseSchema = spec.path("paths").path("/r/p12").path("get")
                .path("responses").path("default").path("content").path("*/*").path("schema");
        assertEquals("#/components/schemas/DataResponse(P12)", responseSchema.path("$ref").asText());

        JsonNode props = spec.path("components").path("schemas").path("P12").path("properties");
        assertEquals("integer", props.path("id").path("type").asText());
        assertEquals("string", props.path("name").path("type").asText());
        assertEquals("#/components/schemas/P13", props.path("toOne").path("$ref").asText());
        assertEquals("array", props.path("toMany").path("type").asText());
        assertEquals("#/components/schemas/P13", props.path("toMany").path("items").path("$ref").asText());
    }

    @Path("r")
    public static class Resource {

        @GET
        @Path("p12")
        public DataResponse<P12> getP12() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @GET
        @Path("non-ag-p2")
        public NonAgP2 getNonAgP2() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }
    }

    public static class P12 {

        @AgId
        public int getA() {
            return -1;
        }

        @AgAttribute
        public String getName() {
            return "";
        }

        @AgRelationship
        public P13 getToOne() {
            return null;
        }

        @AgRelationship
        public Set<P13> getToMany() {
            return Set.of();
        }
    }

    public static class P13 {

        @AgId
        public int getA() {
            return -1;
        }
    }
}
