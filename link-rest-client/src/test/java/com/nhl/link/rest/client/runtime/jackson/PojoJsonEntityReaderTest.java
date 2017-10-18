package com.nhl.link.rest.client.runtime.jackson;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nhl.link.rest.client.it.fixture.T1;
import com.nhl.link.rest.client.runtime.jackson.compiler.PojoJsonEntityReaderCompiler;
import com.nhl.link.rest.runtime.parser.converter.DefaultJsonValueConverterFactoryProvider;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

@Ignore
public class PojoJsonEntityReaderTest {

    PojoJsonEntityReaderCompiler compiler;

    @Before
    public void before() {
        IJsonValueConverterFactory converterFactory
                = new DefaultJsonValueConverterFactoryProvider(Collections.emptyMap()).get();
        compiler = new PojoJsonEntityReaderCompiler(converterFactory);
    }

    @Test
    public void testCompiler() {
        IJsonEntityReader<T1> reader = compiler.compile(T1.class);

        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.set(T1.P_BOOLEAN, JsonNodeFactory.instance.booleanNode(true));
        objectNode.set(T1.P_INTEGER, JsonNodeFactory.instance.numberNode(1));
        objectNode.set(T1.P_STRING, JsonNodeFactory.instance.textNode("abc"));

        T1 t1 = reader.readEntity(objectNode);
        assertEquals(true, t1.isBoolean());
        assertEquals(Integer.valueOf(1), t1.getInteger());
        assertEquals("abc", t1.getString());
    }
}
