package com.nhl.link.rest.client.runtime.jackson.compiler;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nhl.link.rest.client.runtime.jackson.IJsonEntityReader;
import com.nhl.link.rest.runtime.parser.converter.DefaultJsonValueConverterFactoryProvider;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class CayenneJsonEntityReaderCompilerTest {

    JsonEntityReaderCompiler compiler;

    @Before
    public void before() {
        IJsonValueConverterFactory converterFactory
                = new DefaultJsonValueConverterFactoryProvider(Collections.emptyMap()).get();
        compiler = new CayenneJsonEntityReaderCompiler(converterFactory);
    }

    @Test
    public void testCompiler() {
        IJsonEntityReader<T1> reader = compiler.compile(T1.class);

        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.set(T1.P_INT.getName(), JsonNodeFactory.instance.numberNode(1));
        objectNode.set(T1.P_STRING.getName(), JsonNodeFactory.instance.textNode("abc"));

        T1 t1 = reader.readEntity(objectNode);
        assertEquals("abc", t1.getPString());
        assertEquals(Integer.valueOf(1), t1.getPInt());
    }
}
