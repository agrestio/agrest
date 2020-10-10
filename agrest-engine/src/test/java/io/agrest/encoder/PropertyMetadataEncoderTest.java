package io.agrest.encoder;

import io.agrest.meta.*;
import io.agrest.property.PropertyReader;
import io.agrest.resolver.ReaderBasedResolver;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.junit.jupiter.api.Test;

import static io.agrest.encoder.Encoders.toJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertyMetadataEncoderTest {

    final Encoder encoder = PropertyMetadataEncoder.encoder();

    @Test
    public void testEncode_StringAttribute() {
        AgAttribute attribute = new DefaultAgAttribute("prop", String.class, new ASTObjPath("prop"), mock(PropertyReader.class));
        assertEquals("{\"name\":\"prop\",\"type\":\"string\"}", toJson(encoder, attribute));
    }

    @Test
    public void testEncode_ObjectAttribute() {
        AgAttribute attribute = new DefaultAgAttribute("prop", Object.class, new ASTObjPath("prop"), mock(PropertyReader.class));
        assertEquals("{\"name\":\"prop\",\"type\":\"unknown\"}", toJson(encoder, attribute));
    }

    @Test
    public void testEncode_ToOneRelationship() {
        AgEntity<T> target = mock(AgEntity.class);
        when(target.getName()).thenReturn("T");
        AgRelationship r = new DefaultAgRelationship("rel", target, false, new ReaderBasedResolver(mock(PropertyReader.class)));
        assertEquals("{\"name\":\"rel\",\"type\":\"T\",\"relationship\":true}", toJson(encoder, r));
    }

    @Test
    public void testEncode_ToManyRelationship() {
        AgEntity<T> target = mock(AgEntity.class);
        when(target.getName()).thenReturn("T");
        AgRelationship r = new DefaultAgRelationship("rel", target, true, new ReaderBasedResolver(mock(PropertyReader.class)));
        assertEquals("{\"name\":\"rel\",\"type\":\"T\",\"relationship\":true,\"collection\":true}", toJson(encoder, r));
    }

    @Test
    public void testEncode_NotAnAttributeOrRelationship() {
        assertThrows(UnsupportedOperationException.class, () -> toJson(encoder, "iamastring"));
    }

    public static class T {

    }
}
