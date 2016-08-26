package com.nhl.link.rest.client.it.noadapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E4;

public class EntityUtil {

    private static JsonNodeFactory nodeFactory;

    static {
        nodeFactory = new ObjectMapper().getNodeFactory();
    }

    static JsonNode createE2(int id, String name, JsonNode... e3s) {

        ObjectNode e2 = nodeFactory.objectNode();
        e2.set(E2.ID_PK_COLUMN, nodeFactory.numberNode(id));

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

    static JsonNode createE3(int id, String name) {

        ObjectNode e3 = nodeFactory.objectNode();
        e3.set(E3.ID_PK_COLUMN, nodeFactory.numberNode(id));
        e3.set(E3.NAME.getName(), nodeFactory.textNode(name));
        return e3;
    }

    static JsonNode createE4(int id, String cVarchar, int cInt) {

        ObjectNode e4 = nodeFactory.objectNode();
        e4.set(E4.ID_PK_COLUMN, nodeFactory.numberNode(id));
        e4.set(E4.C_INT.getName(), nodeFactory.numberNode(cInt));
        e4.set(E4.C_VARCHAR.getName(), nodeFactory.textNode(cVarchar));
        return e4;
    }
}
