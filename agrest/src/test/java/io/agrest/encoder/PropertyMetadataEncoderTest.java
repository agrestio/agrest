package io.agrest.encoder;

import io.agrest.it.fixture.cayenne.E4;
import io.agrest.meta.DefaultLrAttribute;
import io.agrest.meta.DefaultLrEntity;
import io.agrest.meta.DefaultLrRelationship;
import io.agrest.meta.LrAttribute;
import io.agrest.meta.LrEntity;
import io.agrest.meta.LrRelationship;
import org.junit.Test;

import static io.agrest.encoder.Encoders.toJson;
import static org.junit.Assert.assertEquals;

public class PropertyMetadataEncoderTest {

    private Encoder encoder = PropertyMetadataEncoder.encoder();

    @Test
    public void testEncode_StringAttribute() {
        LrAttribute attribute = new DefaultLrAttribute("prop", String.class);
        assertEquals("{\"name\":\"prop\",\"type\":\"string\"}", toJson(encoder, attribute));
    }

    @Test
    public void testEncode_ObjectAttribute() {
        LrAttribute attribute = new DefaultLrAttribute("prop", Object.class);
        assertEquals("{\"name\":\"prop\",\"type\":\"unknown\"}", toJson(encoder, attribute));
    }

    @Test
    public void testEncode_ToOneRelationship() {
        LrEntity<E4> target = new DefaultLrEntity<>(E4.class);
        LrRelationship r = new DefaultLrRelationship("rel", target, false);
        assertEquals("{\"name\":\"rel\",\"type\":\"E4\",\"relationship\":true}", toJson(encoder, r));
    }

    @Test
    public void testEncode_ToManyRelationship() {
        LrEntity<E4> target = new DefaultLrEntity<>(E4.class);
        LrRelationship r = new DefaultLrRelationship("rel", target, true);
        assertEquals("{\"name\":\"rel\",\"type\":\"E4\",\"relationship\":true,\"collection\":true}", toJson(encoder, r));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEncode_NotAnAttributeOrRelationship() {
        toJson(encoder, "iamastring");
    }
}
