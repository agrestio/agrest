package io.agrest.jaxrs3.openapi;

import io.agrest.DataResponse;
import io.agrest.annotation.AgAttribute;
import io.agrest.jaxrs3.openapi.junit.TestOpenAPIBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Entity_NoPropertyConflictsTest {

    static final OpenAPI oapi = new TestOpenAPIBuilder()
            .addPackage(P7.class)
            .addClass(Resource.class)
            .build();

    @ParameterizedTest
    @ValueSource(strings = {"P7", "P8"})
    @DisplayName("No conflicts in value Schemas of the same type and same name")
    public void testSameProperty(String entity) {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get(entity).getProperties();

        Schema d = props.get("duplicate");
        assertNull(d.getName());
        assertEquals("string", d.getType());
        assertNull(d.getFormat());
    }

    @Test
    @DisplayName("No conflicts in value Schemas of the same type, but different name")
    public void testSimilarProperty() {

        Map<String, Schema> p7Props = oapi.getComponents().getSchemas().get("P7").getProperties();
        Schema otherP7 = p7Props.get("otherP7");
        assertNull(otherP7.getName());
        assertEquals("string", otherP7.getType());
        assertNull(otherP7.getFormat());

        Map<String, Schema> p8Props = oapi.getComponents().getSchemas().get("P8").getProperties();
        Schema otherP8 = p8Props.get("otherP8");
        assertNull(otherP8.getName());
        assertEquals("string", otherP8.getType());
        assertNull(otherP8.getFormat());
    }

    @Path("r")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("p7")
        public DataResponse<P7> getP7() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @GET
        @Path("p8")
        public DataResponse<P8> getP8() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }
    }

    public static class P8 {

        @AgAttribute
        public String getOtherP8() {
            return null;
        }

        @AgAttribute
        public String getDuplicate() {
            return null;
        }
    }

    public static class P7 {

        @AgAttribute
        public String getDuplicate() {
            return null;
        }

        @AgAttribute
        public String getOtherP7() {
            return null;
        }
    }
}
