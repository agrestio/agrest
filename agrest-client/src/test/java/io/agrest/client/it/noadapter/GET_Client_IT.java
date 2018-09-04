package io.agrest.client.it.noadapter;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgREST;
import io.agrest.DataResponse;
import io.agrest.client.ClientDataResponse;
import io.agrest.client.AgRESTClient;
import io.agrest.client.protocol.Expression;
import io.agrest.client.protocol.Include;
import io.agrest.client.protocol.Sort;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E4;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static org.junit.Assert.*;

public class GET_Client_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testClient() {

        insert("e4", "id, c_varchar, c_int", "1, 'xxx', 5");
        insert("e4", "id, c_varchar, c_int", "2, 'yyy', 7");

		ClientDataResponse<JsonNode> response = AgRESTClient.client(target("/e4"))
                .exclude(E4.C_BOOLEAN.getName(), E4.C_DATE.getName(), E4.C_DECIMAL.getName(),
                        E4.C_TIME.getName(), E4.C_TIMESTAMP.getName())
                .get(JsonNode.class);

		assertEquals(Status.OK, response.getStatus());
        assertEquals(2, response.getTotal());

        List<JsonNode> items = response.getData();
        assertNotNull(items);
        assertEquals(2, items.size());

        JsonNode[] expected = new JsonNode[] {EntityUtil.createE4(1, "xxx", 5), EntityUtil.createE4(2, "yyy", 7)};
        assertArrayEquals(expected, items.toArray());
	}

    @Test
    public void testClient_Sort() {

        insert("e4", "id, c_varchar, c_int", "1, 'xxx', 5");
        insert("e4", "id, c_varchar, c_int", "2, 'yyy', 7");

		ClientDataResponse<JsonNode> response = AgRESTClient.client(target("/e4"))
                .exclude(E4.C_BOOLEAN.getName(), E4.C_DATE.getName(), E4.C_DECIMAL.getName(),
                        E4.C_TIME.getName(), E4.C_TIMESTAMP.getName())
                .sort(Sort.property(E4.ID_PK_COLUMN).desc())
                .get(JsonNode.class);

		assertEquals(Status.OK, response.getStatus());
        assertEquals(2, response.getTotal());

        List<JsonNode> items = response.getData();
        assertNotNull(items);
        assertEquals(2, items.size());

        JsonNode[] expected = new JsonNode[] {EntityUtil.createE4(2, "yyy", 7), EntityUtil.createE4(1, "xxx", 5)};
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

		ClientDataResponse<JsonNode> response = AgRESTClient.client(target("/e2"))
                .include(Include.path(E2.E3S.getName())
                        .start(2).limit(2)
                        .sort(Sort.property(E3.NAME.getName()).desc()))
                .exclude(E2.ADDRESS.getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .get(JsonNode.class);

        assertEquals(Status.OK, response.getStatus());
        assertEquals(1, response.getTotal());
        assertEquals(EntityUtil.createE2(1, "xxx", EntityUtil.createE3(7, "ccc"), EntityUtil.createE3(6, "bbb")), response.getData().get(0));
    }

    @Test
    public void testClient_CayenneExpression1() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e3", "id, e2_id, name", "5, 1, 'aaa'");
        insert("e3", "id, e2_id, name", "6, 2, 'bbb'");
        insert("e3", "id, e2_id, name", "7, 1, 'ccc'");

        ClientDataResponse<JsonNode> response = AgRESTClient.client(target("/e2"))
                .exclude(E2.ADDRESS.getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .cayenneExp(Expression.query("name like 'xx%'"))
                .include(Include.path(E2.E3S.getName()).cayenneExp(Expression.query("name = $b").params("ccc")))
                .get(JsonNode.class);

        assertEquals(Status.OK, response.getStatus());
        assertEquals(1, response.getTotal());
        assertEquals(EntityUtil.createE2(1, "xxx", EntityUtil.createE3(7, "ccc")), response.getData().get(0));
    }

    @Test
    public void testClient_CayenneExpression2() {

        insert("e4", "id, c_varchar, c_int, c_boolean", "1, 'xxx', 1, false");
        insert("e4", "id, c_varchar, c_int, c_boolean", "2, 'yyy', 2, false");
        insert("e4", "id, c_varchar, c_int, c_boolean", "3, 'xxz', 3, true");

        ClientDataResponse<JsonNode> response = AgRESTClient.client(target("/e4"))
                .include(E4.ID_PK_COLUMN, E4.C_VARCHAR.getName(), E4.C_INT.getName())
                .cayenneExp(Expression.query("cVarchar like $a and cInt >= $b and cBoolean <> $c")
                                      .param("a", "xx%").param("b", 2).param("c", false))
                .get(JsonNode.class);

        assertEquals(Status.OK, response.getStatus());
        assertEquals(1, response.getTotal());
        assertEquals(EntityUtil.createE4(3, "xxz", 3), response.getData().get(0));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            return AgREST.service(config).select(E2.class).uri(uriInfo).get();
        }

        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@Context UriInfo uriInfo) {
            return AgREST.service(config).select(E4.class).uri(uriInfo).get();
        }
    }
}
