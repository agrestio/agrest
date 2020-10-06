package io.agrest.sencha.runtime.protocol;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.agrest.base.jsonvalueconverter.IJsonValueConverterFactory;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.semantics.IRelationshipMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class SenchaUpdateParserTest {

    final SenchaUpdateParser processor = new SenchaUpdateParser(
            mock(IRelationshipMapper.class),
            mock(IJacksonService.class),
            mock(IJsonValueConverterFactory.class));

    @Test
    public void testIsTempId() {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        assertFalse(processor.isTempId(null));
        assertFalse(processor.isTempId(nodeFactory.objectNode()));
        assertTrue(processor.isTempId(nodeFactory.textNode("My-123")));
        assertFalse(processor.isTempId(nodeFactory.textNode("My-My")));
    }

}
