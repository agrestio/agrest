package io.agrest.client.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;

public class EntityUtil {

    private static JsonNodeFactory nodeFactory;

    static {
        nodeFactory = new ObjectMapper().getNodeFactory();
    }

    public static JsonNode createE2(int id, String name, JsonNode... e3s) {

        ObjectNode e2 = nodeFactory.objectNode();
        e2.set("id", nodeFactory.numberNode(id));

        if (e3s.length > 0) {
            ArrayNode e3sNodes = nodeFactory.arrayNode();
            for (JsonNode e3 : e3s) {
                e3sNodes.add(e3);
            }
            e2.set(E2.E3S.getName(), e3sNodes);
        }

        e2.set(E2.NAME.getName(), nodeFactory.textNode(name));

        return e2;
    }

    public static JsonNode createE3(int id, String name) {

        ObjectNode e3 = nodeFactory.objectNode();
        e3.set("id", nodeFactory.numberNode(id));
        e3.set(E3.NAME.getName(), nodeFactory.textNode(name));
        return e3;
    }

    public static JsonNode createE4(int id, String cVarchar, int cInt) {

        ObjectNode e4 = nodeFactory.objectNode();
        e4.set("id", nodeFactory.numberNode(id));
        e4.set(E4.C_INT.getName(), nodeFactory.numberNode(cInt));
        e4.set(E4.C_VARCHAR.getName(), nodeFactory.textNode(cVarchar));
        return e4;
    }
}
