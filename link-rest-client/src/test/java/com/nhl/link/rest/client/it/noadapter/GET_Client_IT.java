package com.nhl.link.rest.client.it.noadapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nhl.link.rest.client.ClientDataResponse;
import com.nhl.link.rest.client.Include;
import com.nhl.link.rest.client.LinkRestClient;
import com.nhl.link.rest.client.Sort;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.resource.E2Resource;
import com.nhl.link.rest.it.fixture.resource.E4Resource;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response.Status;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GET_Client_IT extends JerseyTestOnDerby {

    private static JsonNodeFactory nodeFactory;

    static {
        nodeFactory = new ObjectMapper().getNodeFactory();
    }

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E2Resource.class);
        context.register(E4Resource.class);
    }

    @Test
    public void testClient() {

        insert("e4", "id, c_varchar, c_int", "1, 'xxx', 5");
        insert("e4", "id, c_varchar, c_int", "2, 'yyy', 7");

		ClientDataResponse<JsonNode> response = LinkRestClient.client(target("/e4"))
                .exclude(E4.C_BOOLEAN.getName(), E4.C_DATE.getName(), E4.C_DECIMAL.getName(),
                        E4.C_TIME.getName(), E4.C_TIMESTAMP.getName())
                .get(JsonNode.class);

		assertEquals(Status.OK, response.getStatus());
        assertEquals(2, response.getTotal());

        List<JsonNode> items = response.getData();
        assertNotNull(items);
        assertEquals(2, items.size());

        JsonNode[] expected = new JsonNode[] {createE4(1, "xxx", 5), createE4(2, "yyy", 7)};
        assertArrayEquals(expected, items.toArray());
	}

    @Test
    public void testClient_Sort() {

        insert("e4", "id, c_varchar, c_int", "1, 'xxx', 5");
        insert("e4", "id, c_varchar, c_int", "2, 'yyy', 7");

		ClientDataResponse<JsonNode> response = LinkRestClient.client(target("/e4"))
                .exclude(E4.C_BOOLEAN.getName(), E4.C_DATE.getName(), E4.C_DECIMAL.getName(),
                        E4.C_TIME.getName(), E4.C_TIMESTAMP.getName())
                .sort(Sort.property(E4.ID_PK_COLUMN).desc())
                .get(JsonNode.class);

		assertEquals(Status.OK, response.getStatus());
        assertEquals(2, response.getTotal());

        List<JsonNode> items = response.getData();
        assertNotNull(items);
        assertEquals(2, items.size());

        JsonNode[] expected = new JsonNode[] {createE4(2, "yyy", 7), createE4(1, "xxx", 5)};
        assertArrayEquals(expected, items.toArray());
	}

    @Test
    public void testClient_Include() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e3", "id, e2_id, name", "5, 1, 'aaa'");
        insert("e3", "id, e2_id, name", "6, 1, 'bbb'");
        insert("e3", "id, e2_id, name", "7, 1, 'ccc'");
        insert("e3", "id, e2_id, name", "8, 1, 'ddd'");
        insert("e3", "id, e2_id, name", "9, 1, 'eee'");

		ClientDataResponse<JsonNode> response = LinkRestClient.client(target("/e2"))
                .include(Include.path(E2.E3S.getName())
                        .start(2).limit(2)
                        .sort(Sort.property(E3.NAME.getName()).desc()))
                .exclude(E2.ADDRESS.getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .get(JsonNode.class);

        assertEquals(Status.OK, response.getStatus());
        assertEquals(1, response.getTotal());
        assertEquals(createE2(1, "xxx", createE3(7, "ccc"), createE3(6, "bbb")), response.getData().get(0));
    }

    @Test
    public void testClient_CayenneExpression() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e3", "id, e2_id, name", "5, 1, 'aaa'");
        insert("e3", "id, e2_id, name", "6, 2, 'bbb'");
        insert("e3", "id, e2_id, name", "7, 1, 'ccc'");

        ClientDataResponse<JsonNode> response = LinkRestClient.client(target("/e2"))
                .exclude(E2.ADDRESS.getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .cayenneExp(E2.NAME.like("xx%"))
                .include(Include.path(E2.E3S.getName()).cayenneExp(E3.NAME.eq("ccc")))
                .get(JsonNode.class);

        assertEquals(Status.OK, response.getStatus());
        assertEquals(1, response.getTotal());
        assertEquals(createE2(1, "xxx", createE3(7, "ccc")), response.getData().get(0));
    }

    private JsonNode createE2(int id, String name, JsonNode... e3s) {

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

    private JsonNode createE3(int id, String name) {

        ObjectNode e3 = nodeFactory.objectNode();
        e3.set(E3.ID_PK_COLUMN, nodeFactory.numberNode(id));
        e3.set(E3.NAME.getName(), nodeFactory.textNode(name));
        return e3;
    }

    private JsonNode createE4(int id, String cVarchar, int cInt) {

        ObjectNode e4 = nodeFactory.objectNode();
        e4.set(E4.ID_PK_COLUMN, nodeFactory.numberNode(id));
        e4.set(E4.C_VARCHAR.getName(), nodeFactory.textNode(cVarchar));
        e4.set(E4.C_INT.getName(), nodeFactory.numberNode(cInt));
        return e4;
    }
}
