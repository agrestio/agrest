package io.agrest.base.jsonvalueconverter;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultJsonValueConverterFactory_PojoTest {

    IJsonValueConverterFactory converterFactory;
    JsonNodeFactory nodeFactory;

    @BeforeEach
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
        assertEquals(true, t1.isBoolean());
        assertEquals(Integer.valueOf(1), t1.getInteger());
        assertEquals("abc", t1.getString());
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
        assertEquals(t3_expected, t3);
    }

    private <T extends Collection> void assertSameContent(T collection, Object... values) {
        assertNotNull(collection);
        assertEquals(values.length, collection.size());
        for (Object value : values) {
            assertTrue(collection.contains(value), "Value is missing from the collection: '" + value + "'");
        }
    }

    public static class T1 {
        public static final String P_BOOLEAN = "boolean";
        public static final String P_INTEGER = "integer";
        public static final String P_STRING = "string";

        private Boolean booleanProperty;
        private Integer integerProperty;
        private String stringProperty;

        public Boolean isBoolean() {
            return booleanProperty;
        }

        public void setBoolean(Boolean booleanProperty) {
            this.booleanProperty = booleanProperty;
        }

        public Integer getInteger() {
            return integerProperty;
        }

        public void setInteger(Integer pInteger) {
            this.integerProperty = pInteger;
        }

        public String getString() {
            return stringProperty;
        }

        public void setString(String pString) {
            this.stringProperty = pString;
        }
    }

    public static class T2 {
        public static final String P_BOOLEANS = "booleans";
        public static final String P_INTEGERS = "integers";
        public static final String P_STRINGS = "strings";

        private Collection<Boolean> booleans;
        private List<Integer> integers;
        private Set<String> strings;

        public Collection<Boolean> getBooleans() {
            return booleans;
        }

        public void setBooleans(Collection<Boolean> booleans) {
            this.booleans = booleans;
        }

        public List<Integer> getIntegers() {
            return integers;
        }

        public void setIntegers(List<Integer> integers) {
            this.integers = integers;
        }

        public Set<String> getStrings() {
            return strings;
        }

        public void setStrings(Set<String> strings) {
            this.strings = strings;
        }
    }

    public static class T3 {
        public static final String P_ID = "id";
        public static final String P_T4S = "t4s";
        public static final String P_T5 = "t5";

        private Integer id;
        private Collection<T4> t4s;
        private T5 t5;

        public T3(Integer id) {
            this.id = Objects.requireNonNull(id);
        }

        public T3() {

        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Collection<T4> getT4s() {
            return t4s;
        }

        public void setT4s(Collection<T4> t4s) {
            this.t4s = t4s;
        }

        public T5 getT5() {
            return t5;
        }

        public void setT5(T5 t5) {
            this.t5 = t5;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;

            T3 t3 = (T3) object;

            if (!id.equals(t3.id)) return false;
            if (t4s != null ? !t4s.containsAll(t3.t4s) : t3.t4s != null) return false;
            return t5 != null ? t5.equals(t3.t5) : t3.t5 == null;

        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + (t4s != null ? t4s.hashCode() : 0);
            result = 31 * result + (t5 != null ? t5.hashCode() : 0);
            return result;
        }
    }

    public static class T4 {
        public static final String P_ID = "id";
        public static final String P_T3 = "t3";

        private Integer id;
        private T3 t3;

        public T4(Integer id) {
            this.id = Objects.requireNonNull(id);
        }

        public T4() {

        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public T3 getT3() {
            return t3;
        }

        public void setT3(T3 t3) {
            this.t3 = t3;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;

            T4 t4 = (T4) object;

            if (!id.equals(t4.id)) return false;
            return t3 != null ? t3.equals(t4.t3) : t4.t3 == null;

        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + (t3 != null ? t3.hashCode() : 0);
            return result;
        }
    }

    public static class T5 {
        public static final String P_ID = "id";
        public static final String P_T3S = "t3s";

        private Integer id;
        private Collection<T3> t3s;

        public T5(Integer id) {
            this.id = Objects.requireNonNull(id);
        }

        public T5() {

        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Collection<T3> getT3s() {
            return t3s;
        }

        public void setT3s(Collection<T3> t3s) {
            this.t3s = t3s;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;

            T5 t5 = (T5) object;

            if (!id.equals(t5.id)) return false;
            return t3s != null ? t3s.containsAll(t5.t3s) : t5.t3s == null;

        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + (t3s != null ? t3s.hashCode() : 0);
            return result;
        }
    }
}
