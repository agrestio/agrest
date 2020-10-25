package io.agrest.openapi;

import io.agrest.DataResponse;
import io.agrest.openapi.unit.OpenAPIBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.example.entity.P1;
import org.example.entity.P3;
import org.example.entity.P4;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import java.util.HashSet;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class SchemaTest {

    static final OpenAPI oapi = new OpenAPIBuilder()
            .addPackage(P1.class)
            .addClass(Resource.class)
            .build();

    @Test
    public void testIdsAndAttributes() {
        Schema p4 = oapi.getComponents().getSchemas().get("P4");
        assertNotNull(p4);

        Map<String, Schema> props = p4.getProperties();
        assertEquals(new HashSet(asList("a", "b", "c")), props.keySet());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b"})
    public void testIntegerTypes(String propertyName) {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema schema = props.get(propertyName);
        assertEquals(propertyName, schema.getName());
        assertEquals("integer", schema.getType());
        assertEquals("int32", schema.getFormat());
    }

    @ParameterizedTest
    @ValueSource(strings = {"c", "d"})
    public void testLongTypes(String propertyName) {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema schema = props.get(propertyName);
        assertEquals(propertyName, schema.getName());
        assertEquals("integer", schema.getType());
        assertEquals("int64", schema.getFormat());
    }

    @ParameterizedTest
    @ValueSource(strings = {"e", "f"})
    public void testFloatTypes(String propertyName) {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema schema = props.get(propertyName);
        assertEquals(propertyName, schema.getName());
        assertEquals("number", schema.getType());
        assertEquals("float", schema.getFormat());
    }

    @ParameterizedTest
    @ValueSource(strings = {"g", "h"})
    public void testDoubleTypes(String propertyName) {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema schema = props.get(propertyName);
        assertEquals(propertyName, schema.getName());
        assertEquals("number", schema.getType());
        assertEquals("double", schema.getFormat());
    }

    @Test
    public void testStringTypes() {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema string = props.get("i");
        assertEquals("i", string.getName());
        assertEquals("string", string.getType());
        assertNull(string.getFormat());
    }

    @Test
    @Disabled
    public void testBinaryStringTypes() {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema base64 = props.get("j");
        assertEquals("j", base64.getName());
        assertEquals("string", base64.getType());
        assertEquals("byte", base64.getFormat());
    }

    @Test
    @Disabled
    public void testDateTimeStringTypes() {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema date = props.get("k");
        assertEquals("k", date.getName());
        assertEquals("string", date.getType());
        assertEquals("date", date.getFormat());

        Schema dateTime = props.get("l");
        assertEquals("l", dateTime.getName());
        assertEquals("string", dateTime.getType());
        assertEquals("date-time", dateTime.getFormat());

        Schema time = props.get("m");
        assertEquals("m", time.getName());
        assertEquals("string", time.getType());
        assertNull(time.getFormat(), "Somehow Open API spec does not define a format for 'time'");
    }

    @ParameterizedTest
    @ValueSource(strings = {"n", "o"})
    public void testBooleanTypes(String propertyName) {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema schema = props.get(propertyName);
        assertEquals(propertyName, schema.getName());
        assertEquals("boolean", schema.getType());
        assertNull(schema.getFormat());
    }

    @Path("r")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        public DataResponse<P3> getP3() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }

        @GET
        public DataResponse<P4> getP4() {
            throw new UnsupportedOperationException("endpoint logic is irrelevant for the test");
        }
    }
}
