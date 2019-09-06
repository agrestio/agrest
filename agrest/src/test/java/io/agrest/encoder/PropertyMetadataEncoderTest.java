package io.agrest.encoder;

import io.agrest.it.fixture.cayenne.E4;
import io.agrest.meta.AgEntity;
import io.agrest.meta.DefaultAgAttribute;
import io.agrest.meta.DefaultAgEntity;
import io.agrest.meta.DefaultAgRelationship;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgRelationship;
import org.junit.Test;

import static io.agrest.encoder.Encoders.toJson;
import static org.junit.Assert.assertEquals;

public class PropertyMetadataEncoderTest {

    private Encoder encoder = PropertyMetadataEncoder.encoder();

    @Test
    public void testEncode_StringAttribute() {
        AgAttribute attribute = new DefaultAgAttribute("prop", String.class);
        assertEquals("{\"name\":\"prop\",\"type\":\"string\"}", toJson(encoder, attribute));
    }

    @Test
    public void testEncode_ObjectAttribute() {
        AgAttribute attribute = new DefaultAgAttribute("prop", Object.class);
        assertEquals("{\"name\":\"prop\",\"type\":\"unknown\"}", toJson(encoder, attribute));
    }

    @Test
    public void testEncode_ToOneRelationship() {
        AgEntity<E4> target = new DefaultAgEntity<>("E4", E4.class);
        AgRelationship r = new DefaultAgRelationship("rel", target, false);
        assertEquals("{\"name\":\"rel\",\"type\":\"E4\",\"relationship\":true}", toJson(encoder, r));
    }

    @Test
    public void testEncode_ToManyRelationship() {
        AgEntity<E4> target = new DefaultAgEntity<>("E4", E4.class);
        AgRelationship r = new DefaultAgRelationship("rel", target, true);
        assertEquals("{\"name\":\"rel\",\"type\":\"E4\",\"relationship\":true,\"collection\":true}", toJson(encoder, r));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEncode_NotAnAttributeOrRelationship() {
        toJson(encoder, "iamastring");
    }
}
