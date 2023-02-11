package io.agrest.jaxrs2.openapi;

import io.agrest.DataResponse;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.jaxrs2.openapi.junit.TestOpenAPIBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EntityTest {

    static final OpenAPI oapi = new TestOpenAPIBuilder()
            .addPackage(P3.class)
            .addClass(Resource.class)
            .build();

    @Test
    public void testPropertiesPresentAndOrdered() {
        Schema p4 = oapi.getComponents().getSchemas().get("P4");
        assertNotNull(p4);

        Map<String, Schema> props = p4.getProperties();

        // checking both presence and ordering
        assertEquals(List.of("id", "b", "p5", "p6s", "z"), List.copyOf(props.keySet()));

        Schema id = props.get("id");
        assertNotNull(id);
        assertEquals("integer", id.getType());
        assertEquals("int32", id.getFormat());

        Schema b = props.get("b");
        assertNotNull(b);
        assertEquals("integer", b.getType());
        assertEquals("int32", b.getFormat());

        Schema z = props.get("z");
        assertNotNull(z);
        assertEquals("string", z.getType());

        Schema p5 = props.get("p5");
        assertNotNull(p5);
        assertNull(p5.getName());
        assertNull(p5.getType());
        assertNull(p5.getFormat());
        assertEquals("#/components/schemas/P5", p5.get$ref());

        Schema p6s = props.get("p6s");
        assertNotNull(p6s);
        assertEquals("array", p6s.getType());

        Schema p6sItems = ((ArraySchema) p6s).getItems();
        assertNotNull(p6sItems);
        assertNull(p6sItems.getName());
        assertNull(p6sItems.getType());
        assertNull(p6sItems.getFormat());
        assertEquals("#/components/schemas/P6", p6sItems.get$ref());
    }

    @Test
    public void testProperties_FilterByAccess() {
        Schema p8Update = oapi.getComponents().getSchemas().get("P8");
        assertNotNull(p8Update);

        Map<String, Schema> props = p8Update.getProperties();

        assertEquals(List.of("b"), List.copyOf(props.keySet()));

        Schema b = props.get("b");
        assertNotNull(b);
        assertEquals("integer", b.getType());
        assertEquals("int32", b.getFormat());
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

        @GET
        public DataResponse<P8> getP8() {
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

        // intentionally declare properties in non-alphabetic order

        @AgAttribute
        public String getZ() {
            return "";
        }

        @AgAttribute
        public int getB() {
            return -1;
        }

        @AgId
        public int getA() {
            return -1;
        }

        @AgRelationship
        public Set<EntityUpdateTest.P6> getP6s() {
            return Set.of();
        }

        @AgRelationship
        public EntityUpdateTest.P5 getP5() {
            return null;
        }
    }

    public static class P5 {
        @AgId
        public long getA() {
            return -1L;
        }

        @AgAttribute
        public int getB() {
            return -1;
        }
    }

    public static class P6 {
        @AgId
        public String getA() {
            return "";
        }

        @AgAttribute
        public int getB() {
            return -1;
        }
    }

    public static class P8 {

        @AgAttribute
        public int getB() {
            return -1;
        }

        @AgAttribute(readable = false)
        public String getZ() {
            return "";
        }

        @AgId(readable = false)
        public int getA() {
            return -1;
        }

        @AgRelationship(readable = false)
        public Set<P6> getP6s() {
            return Set.of();
        }

        @AgRelationship(readable = false)
        public P5 getP5() {
            return null;
        }
    }
}
