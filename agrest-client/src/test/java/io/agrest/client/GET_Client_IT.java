package io.agrest.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.client.protocol.Expression;
import io.agrest.client.protocol.Include;
import io.agrest.client.protocol.Sort;
import io.agrest.client.unit.ClientDbTest;
import io.agrest.client.unit.EntityUtil;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GET_Client_IT extends ClientDbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class)
            .build();

    @Test
    public void testClient() {

        tester.e4().insertColumns("id", "c_varchar", "c_int")
                .values(1, "xxx", 5)
                .values(2, "yyy", 7)
                .exec();

        ClientDataResponse<JsonNode> response = client(tester, "/e4")
                .exclude(E4.C_BOOLEAN.getName(),
                        E4.C_DATE.getName(),
                        E4.C_DECIMAL.getName(),
                        E4.C_TIME.getName(),
                        E4.C_TIMESTAMP.getName())
                .get(JsonNode.class);

        assertEquals(Status.OK, response.getStatus());
        assertEquals(2, response.getTotal());

        List<JsonNode> items = response.getData();
        assertNotNull(items);
        assertEquals(2, items.size());

        JsonNode[] expected = new JsonNode[]{EntityUtil.createE4(1, "xxx", 5), EntityUtil.createE4(2, "yyy", 7)};
        assertArrayEquals(expected, items.toArray());
    }

    @Test
    public void testClient_Sort() {

        tester.e4().insertColumns("id", "c_varchar", "c_int")
                .values(1, "xxx", 5)
                .values(2, "yyy", 7)
                .exec();

        ClientDataResponse<JsonNode> response = client(tester, "/e4")
                .exclude(E4.C_BOOLEAN.getName(),
                        E4.C_DATE.getName(),
                        E4.C_DECIMAL.getName(),
                        E4.C_TIME.getName(),
                        E4.C_TIMESTAMP.getName())
                .sort(Sort.property(E4.ID_PK_COLUMN).desc())
                .get(JsonNode.class);

        assertEquals(Status.OK, response.getStatus());
        assertEquals(2, response.getTotal());

        List<JsonNode> items = response.getData();
        assertNotNull(items);
        assertEquals(2, items.size());

        JsonNode[] expected = new JsonNode[]{EntityUtil.createE4(2, "yyy", 7), EntityUtil.createE4(1, "xxx", 5)};
        assertArrayEquals(expected, items.toArray());
    }

    @Test
    public void testClient_Include() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(5, "aaa", 1)
                .values(6, "bbb", 1)
                .values(7, "ccc", 1)
                .values(8, "ddd", 1)
                .values(9, "eee", 1).exec();

        ClientDataResponse<JsonNode> response = client(tester, "/e2")
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

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(5, "aaa", 1)
                .values(6, "bbb", 2)
                .values(7, "ccc", 1).exec();

        ClientDataResponse<JsonNode> response = client(tester, "/e2")
                .exclude(E2.ADDRESS.getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .exp(Expression.query("name like 'xx%'"))
                .include(Include.path(E2.E3S.getName()).exp(Expression.query("name = $b").params("ccc")))
                .get(JsonNode.class);

        assertEquals(Status.OK, response.getStatus());
        assertEquals(1, response.getTotal());
        assertEquals(EntityUtil.createE2(1, "xxx", EntityUtil.createE3(7, "ccc")), response.getData().get(0));
    }

    @Test
    public void testClient_CayenneExpression2() {

        tester.e4().insertColumns("id", "c_varchar", "c_int", "c_boolean")
                .values(1, "xxx", 1, false)
                .values(2, "yyy", 2, false)
                .values(3, "xxz", 3, true)
                .exec();

        ClientDataResponse<JsonNode> response = client(tester, "/e4")
                .include(E4.ID_PK_COLUMN, E4.C_VARCHAR.getName(), E4.C_INT.getName())
                .exp(Expression.query("cVarchar like $a and cInt >= $b and cBoolean <> $c")
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
            return Ag.service(config).select(E2.class).uri(uriInfo).get();
        }

        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E4.class).uri(uriInfo).get();
        }
    }
}
