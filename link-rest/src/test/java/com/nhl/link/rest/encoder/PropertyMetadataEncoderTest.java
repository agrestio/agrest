package com.nhl.link.rest.encoder;

import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.meta.DefaultLrAttribute;
import com.nhl.link.rest.meta.DefaultLrEntity;
import com.nhl.link.rest.meta.DefaultLrRelationship;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import org.junit.Test;

import static com.nhl.link.rest.encoder.Encoders.toJson;
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
