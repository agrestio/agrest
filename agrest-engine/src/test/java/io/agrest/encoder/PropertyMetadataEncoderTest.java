package io.agrest.encoder;

import io.agrest.meta.*;
import io.agrest.property.PropertyReader;
import io.agrest.resolver.ReaderBasedResolver;
import io.agrest.runtime.encoder.Encoders;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Deprecated
public class PropertyMetadataEncoderTest {

    final Encoder encoder = PropertyMetadataEncoder.encoder();

    @Test
    public void testEncode_StringAttribute() {
        AgAttribute attribute = new DefaultAgAttribute("prop", String.class, true, true, mock(PropertyReader.class));
        assertEquals("{\"name\":\"prop\",\"type\":\"string\"}", Encoders.toJson(encoder, attribute));
    }

    @Test
    public void testEncode_ObjectAttribute() {
        AgAttribute attribute = new DefaultAgAttribute("prop", Object.class, true, true, mock(PropertyReader.class));
        assertEquals("{\"name\":\"prop\",\"type\":\"unknown\"}",  Encoders.toJson(encoder, attribute));
    }

    @Test
    public void testEncode_ToOneRelationship() {
        AgEntity<T> target = mock(AgEntity.class);
        when(target.getName()).thenReturn("T");
        AgRelationship r = new DefaultAgRelationship("rel", target, false, true, true, new ReaderBasedResolver(mock(PropertyReader.class)));
        assertEquals("{\"name\":\"rel\",\"type\":\"T\",\"relationship\":true}",  Encoders.toJson(encoder, r));
    }

    @Test
    public void testEncode_ToManyRelationship() {
        AgEntity<T> target = mock(AgEntity.class);
        when(target.getName()).thenReturn("T");
        AgRelationship r = new DefaultAgRelationship("rel", target, true, true, true, new ReaderBasedResolver(mock(PropertyReader.class)));
        assertEquals("{\"name\":\"rel\",\"type\":\"T\",\"relationship\":true,\"collection\":true}",  Encoders.toJson(encoder, r));
    }

    @Test
    public void testEncode_NotAnAttributeOrRelationship() {
        assertThrows(UnsupportedOperationException.class, () ->  Encoders.toJson(encoder, "iamastring"));
    }

    public static class T {

    }
}
