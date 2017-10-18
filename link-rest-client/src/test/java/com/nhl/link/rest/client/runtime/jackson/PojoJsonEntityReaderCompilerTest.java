package com.nhl.link.rest.client.runtime.jackson;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nhl.link.rest.client.it.fixture.T1;
import com.nhl.link.rest.client.it.fixture.T2;
import com.nhl.link.rest.client.runtime.jackson.compiler.PojoJsonEntityReaderCompiler;
import com.nhl.link.rest.runtime.parser.converter.DefaultJsonValueConverterFactoryProvider;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PojoJsonEntityReaderCompilerTest {

    PojoJsonEntityReaderCompiler compiler;
    JsonNodeFactory nodeFactory;

    @Before
    public void before() {
        IJsonValueConverterFactory converterFactory
                = new DefaultJsonValueConverterFactoryProvider(Collections.emptyMap()).get();
        compiler = new PojoJsonEntityReaderCompiler(converterFactory);
        nodeFactory = JsonNodeFactory.instance;
    }

    private <T> IJsonEntityReader<T> compile(Class<T> type) {
        return compiler.compile(type);
    }

    private JsonNodeFactory nodeFactory() {
        return nodeFactory;
    }

    @Test
    public void testCompiler_SimpleProperties() {
        IJsonEntityReader<T1> reader = compile(T1.class);

        ObjectNode objectNode = nodeFactory().objectNode();
        objectNode.set(T1.P_BOOLEAN, nodeFactory().booleanNode(true));
        objectNode.set(T1.P_INTEGER, nodeFactory().numberNode(1));
        objectNode.set(T1.P_STRING, nodeFactory().textNode("abc"));

        T1 t1 = reader.readEntity(objectNode);
        assertEquals(true, t1.isBoolean());
        assertEquals(Integer.valueOf(1), t1.getInteger());
        assertEquals("abc", t1.getString());
    }

    @Test
    public void testCompiler_CollectionProperties() {
        IJsonEntityReader<T2> reader = compile(T2.class);

        ObjectNode objectNode = nodeFactory().objectNode();
        objectNode.set(T2.P_BOOLEANS, nodeFactory().arrayNode().add(true).add(false));
        objectNode.set(T2.P_INTEGERS, nodeFactory().arrayNode().add(1).add(2).add(3));
        objectNode.set(T2.P_STRINGS, nodeFactory().arrayNode().add("a").add("b").add("c"));

        T2 t2 = reader.readEntity(objectNode);
        assertSameContent(t2.getBooleans(), true, false);
        assertSameContent(t2.getIntegers(), 1, 2, 3);
        assertSameContent(t2.getStrings(), "a", "b", "c");
    }

    private <T extends Collection> void assertSameContent(T collection, Object... values) {
        assertNotNull(collection);
        assertEquals(values.length, collection.size());
        for (Object value : values) {
            assertTrue("Value is missing from the collection: '" + value + "'", collection.contains(value));
        }
    }
}
