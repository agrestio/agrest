package com.nhl.link.rest.encoder;

import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.meta.DefaultLrAttribute;
import com.nhl.link.rest.meta.DefaultLrEntity;
import com.nhl.link.rest.meta.DefaultLrRelationship;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PropertyMetadataEncoderTest {

    private static IJacksonService JACKSON = new JacksonService();

    private static String toJson(Object value) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            JACKSON.outputJson(g ->
                    PropertyMetadataEncoder.encoder().encode(null, value, g), out);
        } catch (IOException e) {
            fail("Encoding error: " + e.getMessage());
        }

        return new String(out.toByteArray());
    }

    @Test
    public void testEncode_StringAttribute() {
        LrAttribute attribute = new DefaultLrAttribute("prop", String.class);
        assertEquals("{\"name\":\"prop\",\"type\":\"string\"}", toJson(attribute));
    }

    @Test
    public void testEncode_ObjectAttribute() {
        LrAttribute attribute = new DefaultLrAttribute("prop", Object.class);
        assertEquals("{\"name\":\"prop\",\"type\":\"unknown\"}", toJson(attribute));
    }

    @Test
    public void testEncode_ToOneRelationship() {
        LrEntity<E4> target = new DefaultLrEntity<>(E4.class);
        LrRelationship r = new DefaultLrRelationship("rel", target, false);
        assertEquals("{\"name\":\"rel\",\"type\":\"E4\",\"relationship\":true}", toJson(r));
    }

    @Test
    public void testEncode_ToManyRelationship() {
        LrEntity<E4> target = new DefaultLrEntity<>(E4.class);
        LrRelationship r = new DefaultLrRelationship("rel", target, true);
        assertEquals("{\"name\":\"rel\",\"type\":\"E4\",\"relationship\":true,\"collection\":true}", toJson(r));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEncode_NotAnAttributeOrRelationship() {
        toJson("iamastring");
    }
}
