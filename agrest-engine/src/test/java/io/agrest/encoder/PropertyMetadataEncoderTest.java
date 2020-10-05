package io.agrest.encoder;

import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.DefaultAgAttribute;
import io.agrest.meta.DefaultAgRelationship;
import io.agrest.property.BeanPropertyReader;
import io.agrest.resolver.ReaderBasedResolver;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.junit.jupiter.api.Test;

import static io.agrest.encoder.Encoders.toJson;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertyMetadataEncoderTest {

    private Encoder encoder = PropertyMetadataEncoder.encoder();

    @Test
    public void testEncode_StringAttribute() {
        AgAttribute attribute = new DefaultAgAttribute("prop", String.class, new ASTObjPath("prop"), BeanPropertyReader.reader());
        assertEquals("{\"name\":\"prop\",\"type\":\"string\"}", toJson(encoder, attribute));
    }

    @Test
    public void testEncode_ObjectAttribute() {
        AgAttribute attribute = new DefaultAgAttribute("prop", Object.class, new ASTObjPath("prop"), BeanPropertyReader.reader());
        assertEquals("{\"name\":\"prop\",\"type\":\"unknown\"}", toJson(encoder, attribute));
    }

    @Test
    public void testEncode_ToOneRelationship() {
        AgEntity<T> target = mock(AgEntity.class);
        when(target.getName()).thenReturn("T");
        AgRelationship r = new DefaultAgRelationship("rel", target, false, new ReaderBasedResolver(BeanPropertyReader.reader()));
        assertEquals("{\"name\":\"rel\",\"type\":\"T\",\"relationship\":true}", toJson(encoder, r));
    }

    @Test
    public void testEncode_ToManyRelationship() {
        AgEntity<T> target = mock(AgEntity.class);
        when(target.getName()).thenReturn("T");
        AgRelationship r = new DefaultAgRelationship("rel", target, true, new ReaderBasedResolver(BeanPropertyReader.reader()));
        assertEquals("{\"name\":\"rel\",\"type\":\"T\",\"relationship\":true,\"collection\":true}", toJson(encoder, r));
    }

    @Test
    public void testEncode_NotAnAttributeOrRelationship() {
        assertThrows(UnsupportedOperationException.class, () -> toJson(encoder, "iamastring"));
    }

    public class T {

    }
}
