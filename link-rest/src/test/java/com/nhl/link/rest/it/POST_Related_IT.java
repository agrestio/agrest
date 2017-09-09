package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E12;
import com.nhl.link.rest.it.fixture.cayenne.E12E13;
import com.nhl.link.rest.it.fixture.cayenne.E17;
import com.nhl.link.rest.it.fixture.cayenne.E18;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.query.SQLSelect;
import org.junit.Test;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class POST_Related_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testRelate_ToMany_New() {

        insert("e2", "id, name", "24, 'xxx'");

        Response response = target("/e2/24/e3s")
                .request()
                .post(Entity.json("{\"name\":\"zzz\"}"));

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"data\":[{REPLACED_ID,\"name\":\"zzz\",\"phoneNumber\":null}],\"total\":1}",
                response.readEntity(String.class).replaceFirst("\"id\":[\\d]+", "REPLACED_ID"));

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e3"));

        DataRow row = SQLSelect.dataRowQuery("SELECT e2_id, name FROM utest.e3").lowerColumnNames()
                .selectOne(newContext());
        assertEquals("zzz", row.get("name"));
        assertEquals(24, row.get("e2_id"));
    }

    @Test
    public void testRelate_ToMany_New_CompoundId() {

        insert("e17", "id1, id2, name", "1, 1, 'aaa'");

        Response response = target("/e17/e18s")
                .matrixParam("parentId1", 1)
                .matrixParam("parentId2", 1)
                .request()
                .post(Entity.json("{\"name\":\"xxx\"}"));

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"data\":[{REPLACED_ID,\"name\":\"xxx\"}],\"total\":1}",
                response.readEntity(String.class).replaceFirst("\"id\":[\\d]+", "REPLACED_ID"));

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e18"));

        DataRow row = SQLSelect.dataRowQuery("SELECT e17_id1, e17_id2, name FROM utest.e18").lowerColumnNames()
                .selectOne(newContext());
        assertEquals("xxx", row.get("name"));
        assertEquals(1, row.get("e17_id1"));
        assertEquals(1, row.get("e17_id2"));
    }

    @Test
    public void testRelate_ToMany_MixedCollection() {

        insert("e2", "id, name", "15, 'xxx'");
        insert("e2", "id, name", "16, 'xxx'");

        insert("e3", "id, name, e2_id", "7, 'zzz', 16");
        insert("e3", "id, name, e2_id", "8, 'yyy', 15");
        insert("e3", "id, name, e2_id", "9, 'aaa', 15");

        Response r1 = target("/e2/15/e3s")
                .request()
                .post(Entity.json("[ {\"id\":8,\"name\":\"123\"}, {\"name\":\"newname\"} ]"));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":8,\"name\":\"123\",\"phoneNumber\":null},"
                        + "{\"id\":1,\"name\":\"newname\",\"phoneNumber\":null}],\"total\":2}",
                r1.readEntity(String.class));
        assertEquals(4, intForQuery("SELECT count(1) FROM utest.e3"));
        assertEquals(3, intForQuery("SELECT count(1) FROM utest.e3 WHERE e2_id = 15"));

        // testing non-idempotency

        Response r2 = target("/e2/15/e3s")
                .request()
                .post(Entity.json("[ {\"id\":8,\"name\":\"123\"}, {\"name\":\"newname\"} ]"));

        assertEquals(Status.OK.getStatusCode(), r2.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":8,\"name\":\"123\",\"phoneNumber\":null},"
                        + "{\"id\":2,\"name\":\"newname\",\"phoneNumber\":null}],\"total\":2}",
                r2.readEntity(String.class));
        assertEquals(5, intForQuery("SELECT count(1) FROM utest.e3"));
        assertEquals(4, intForQuery("SELECT count(1) FROM utest.e3 WHERE e2_id = 15"));
    }

    @Test
    public void testPOST_ToManyJoin() {

        insert("e12", "id", "11");
        insert("e12", "id", "12");
        insert("e13", "id", "14");
        insert("e13", "id", "15");
        insert("e13", "id", "16");

        Response response = target("/e12/12/e1213")
                .queryParam("exclude", "id")
                .request()
                .post(Entity.json("[{\"e13\":15},{\"e13\":14}]"));

        assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals("{\"data\":[{},{}],\"total\":2}", response.readEntity(String.class));

        assertEquals(2, intForQuery("SELECT count(1) FROM utest.e12_e13"));
        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e12_e13 " + "WHERE e12_id = 12 AND e13_id = 14"));
        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e12_e13 " + "WHERE e12_id = 12 AND e13_id = 15"));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e2/{id}/e3s")
        public DataResponse<E3> createOrUpdateE3sOfE2(@PathParam("id") int id, String targetData) {
            return LinkRest.createOrUpdate(E3.class, config).toManyParent(E2.class, id, E2.E3S).syncAndSelect(targetData);
        }

        @POST
        @Path("e12/{id}/e1213")
        public DataResponse<E12E13> create_Joins(@PathParam("id") int id, @Context UriInfo info, String entityData) {
            return LinkRest.create(E12E13.class, config).toManyParent(E12.class, id, E12.E1213).uri(info)
                    .syncAndSelect(entityData);
        }

        @POST
        @Path("e17/e18s")
        public DataResponse<E18> createOrUpdateE18s(
                @Context UriInfo uriInfo,
                @MatrixParam("parentId1") Integer parentId1,
                @MatrixParam("parentId2") Integer parentId2,
                String targetData) {

            Map<String, Object> parentIds = new HashMap<>();
            parentIds.put(E17.ID1_PK_COLUMN, parentId1);
            parentIds.put(E17.ID2_PK_COLUMN, parentId2);

            return LinkRest.createOrUpdate(E18.class, config).toManyParent(E17.class, parentIds, E17.E18S).uri(uriInfo)
                    .syncAndSelect(targetData);
        }
    }

}
