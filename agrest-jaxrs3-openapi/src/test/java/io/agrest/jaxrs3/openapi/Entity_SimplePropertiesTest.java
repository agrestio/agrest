package io.agrest.jaxrs3.openapi;

import io.agrest.DataResponse;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.jaxrs3.openapi.junit.TestOpenAPIBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Entity_SimplePropertiesTest {

    static final OpenAPI oapi = new TestOpenAPIBuilder()
            .addPackage(P3.class)
            .addClass(Resource.class)
            .build();

    @Test
    public void testIdsAndAttributes() {
        Schema p4 = oapi.getComponents().getSchemas().get("P4");
        assertNotNull(p4);

        Map<String, Schema> props = p4.getProperties();

        // checking both presence and the ordering
        assertEquals(List.of("id", "b", "c"), List.copyOf(props.keySet()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b"})
    public void testIntegerTypes(String propertyName) {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema schema = props.get(propertyName);
        assertNull(schema.getName());
        assertEquals("integer", schema.getType());
        assertEquals("int32", schema.getFormat());
    }

    @ParameterizedTest
    @ValueSource(strings = {"c", "d"})
    public void testLongTypes(String propertyName) {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema schema = props.get(propertyName);
        assertNull(schema.getName());
        assertEquals("integer", schema.getType());
        assertEquals("int64", schema.getFormat());
    }

    @ParameterizedTest
    @ValueSource(strings = {"e", "f"})
    public void testFloatTypes(String propertyName) {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema schema = props.get(propertyName);
        assertNull(schema.getName());
        assertEquals("number", schema.getType());
        assertEquals("float", schema.getFormat());
    }

    @ParameterizedTest
    @ValueSource(strings = {"g", "h"})
    public void testDoubleTypes(String propertyName) {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema schema = props.get(propertyName);
        assertNull(schema.getName());
        assertEquals("number", schema.getType());
        assertEquals("double", schema.getFormat());
    }

    @Test
    public void testStringTypes() {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema string = props.get("i");
        assertNull(string.getName());
        assertEquals("string", string.getType());
        assertNull(string.getFormat());
    }

    @Test
    public void testBinaryStringTypes() {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema base64 = props.get("j");
        assertNull(base64.getName());
        assertEquals("string", base64.getType());
        assertEquals("byte", base64.getFormat());
    }

    @Test
    public void testDateTimeStringTypes() {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema date = props.get("k");
        assertNull(date.getName());
        assertEquals("string", date.getType());
        assertEquals("date", date.getFormat());

        Schema dateTime = props.get("l");
        assertNull(dateTime.getName());
        assertEquals("string", dateTime.getType());
        assertEquals("date-time", dateTime.getFormat());

        Schema time = props.get("m");
        assertNull(time.getName());
        assertEquals("string", time.getType());
        assertEquals("partial-time", time.getFormat());
    }

    @ParameterizedTest
    @ValueSource(strings = {"n", "o"})
    public void testBooleanTypes(String propertyName) {

        Map<String, Schema> props = oapi.getComponents().getSchemas().get("P3").getProperties();

        Schema schema = props.get(propertyName);
        assertNull(schema.getName());
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

    public static class P3 {

        @AgAttribute
        public Integer getA() {
            return null;
        }

        @AgAttribute
        public int getB() {
            return -1;
        }

        @AgAttribute
        public Long getC() {
            return null;
        }

        @AgAttribute
        public long getD() {
            return 0;
        }

        @AgAttribute
        public Float getE() {
            return null;
        }

        @AgAttribute
        public float getF() {
            return 0;
        }

        @AgAttribute
        public Double getG() {
            return null;
        }

        @AgAttribute
        public double getH() {
            return 0;
        }

        @AgAttribute
        public String getI() {
            return "";
        }

        @AgAttribute
        public byte[] getJ() {
            return null;
        }

        @AgAttribute
        public LocalDate getK() {
            return null;
        }

        @AgAttribute
        public LocalDateTime getL() {
            return null;
        }

        @AgAttribute
        public LocalTime getM() {
            return null;
        }

        @AgAttribute
        public Boolean getN() {
            return null;
        }

        @AgAttribute
        public boolean getO() {
            return false;
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
    }
}
