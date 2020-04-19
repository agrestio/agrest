package io.agrest.base.jsonvalueconverter;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.agrest.it.fixture.T1;
import io.agrest.it.fixture.T2;
import io.agrest.it.fixture.T3;
import io.agrest.it.fixture.T4;
import io.agrest.it.fixture.T5;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DefaultJsonValueConverterFactory_PojoTest {

    IJsonValueConverterFactory converterFactory;
    JsonNodeFactory nodeFactory;

    @Before
    public void before() {
        converterFactory = new DefaultJsonValueConverterFactoryProvider(Collections.emptyMap()).get();
        nodeFactory = JsonNodeFactory.instance;
    }

    private <T> JsonValueConverter<T> compile(Class<T> type) {
        return converterFactory.typedConverter(type)
                .orElseThrow(() -> new RuntimeException("Can't create converter for type: " + type.getName()));
    }

    private JsonNodeFactory nodeFactory() {
        return nodeFactory;
    }

    @Test
    public void testCompiler_SimpleProperties() {
        JsonValueConverter<T1> reader = compile(T1.class);

        ObjectNode objectNode = nodeFactory().objectNode();
        objectNode.set(T1.P_BOOLEAN, nodeFactory().booleanNode(true));
        objectNode.set(T1.P_INTEGER, nodeFactory().numberNode(1));
        objectNode.set(T1.P_STRING, nodeFactory().textNode("abc"));

        T1 t1 = reader.value(objectNode);
        Assert.assertEquals(true, t1.isBoolean());
        Assert.assertEquals(Integer.valueOf(1), t1.getInteger());
        Assert.assertEquals("abc", t1.getString());
    }

    @Test
    public void testCompiler_CollectionProperties() {
        JsonValueConverter<T2> reader = compile(T2.class);

        ObjectNode objectNode = nodeFactory().objectNode();
        objectNode.set(T2.P_BOOLEANS, nodeFactory().arrayNode().add(true).add(false));
        objectNode.set(T2.P_INTEGERS, nodeFactory().arrayNode().add(1).add(2).add(3));
        objectNode.set(T2.P_STRINGS, nodeFactory().arrayNode().add("a").add("b").add("c"));

        T2 t2 = reader.value(objectNode);
        assertSameContent(t2.getBooleans(), true, false);
        assertSameContent(t2.getIntegers(), 1, 2, 3);
        assertSameContent(t2.getStrings(), "a", "b", "c");
    }

    @Test
    public void testCompiler_RelationshipProperties() {
        JsonValueConverter<T3> reader = compile(T3.class);

        ObjectNode t3_objectNode_Hollow = nodeFactory().objectNode();
        t3_objectNode_Hollow.set(T3.P_ID, nodeFactory().numberNode(11));

        ObjectNode t4_objectNode1 = nodeFactory().objectNode();
        t4_objectNode1.set(T4.P_ID, nodeFactory().numberNode(41));
        t4_objectNode1.set(T4.P_T3, t3_objectNode_Hollow);

        ObjectNode t4_objectNode2 = nodeFactory().objectNode();
        t4_objectNode2.set(T4.P_ID, nodeFactory().numberNode(42));

        ObjectNode t5_objectNode = nodeFactory().objectNode();
        t5_objectNode.set(T5.P_ID, nodeFactory().numberNode(51));
        t5_objectNode.set(T5.P_T3S, nodeFactory().arrayNode().add(t3_objectNode_Hollow));

        ObjectNode t3_objectNode = nodeFactory().objectNode();
        t3_objectNode.set(T3.P_ID, nodeFactory().numberNode(11));
        t3_objectNode.set(T3.P_T4S, nodeFactory().arrayNode().add(t4_objectNode1).add(t4_objectNode2));
        t3_objectNode.set(T3.P_T5, t5_objectNode);

        T3 t3_expected = new T3(11);
        T4 t4_1 = new T4(41);
        t4_1.setT3(new T3(11));
        T4 t4_2 = new T4(42);
        t3_expected.setT4s(Arrays.asList(t4_1, t4_2));
        T5 t5 = new T5(51);
        t5.setT3s(Collections.singleton(new T3(11)));
        t3_expected.setT5(t5);

        T3 t3 = reader.value(t3_objectNode);
        Assert.assertEquals(t3_expected, t3);
    }

    private <T extends Collection> void assertSameContent(T collection, Object... values) {
        assertNotNull(collection);
        assertEquals(values.length, collection.size());
        for (Object value : values) {
            assertTrue("Value is missing from the collection: '" + value + "'", collection.contains(value));
        }
    }
}
