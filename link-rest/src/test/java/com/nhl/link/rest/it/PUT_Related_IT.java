package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E1;
import com.nhl.link.rest.it.fixture.cayenne.E12;
import com.nhl.link.rest.it.fixture.cayenne.E12E13;
import com.nhl.link.rest.it.fixture.cayenne.E15;
import com.nhl.link.rest.it.fixture.cayenne.E15E1;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E7;
import com.nhl.link.rest.it.fixture.cayenne.E8;
import com.nhl.link.rest.it.fixture.cayenne.E9;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.assertEquals;

public class PUT_Related_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testRelate_EmptyPutWithID() {

        performQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (24, 'xxx')"));
        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (7, 'zzz')"));
        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (8, 'yyy')"));

        // POST with empty body ... how bad is that?
        Response r1 = target("/e3/8/e2/24").request().put(Entity.entity("", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":24,\"address\":null,\"name\":\"xxx\"}],\"total\":1}",
                r1.readEntity(String.class));

        assertEquals("yyy", stringForQuery("SELECT name FROM utest.e3 WHERE e2_id = 24"));
    }

    @Test
    public void testRelate_ValidRel_ToOne_Existing() {

        performQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (24, 'xxx')"));
        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (7, 'zzz')"));
        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (8, 'yyy')"));

        Response r1 = target("/e3/8/e2/24").request().put(Entity.entity("{}", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":24,\"address\":null,\"name\":\"xxx\"}],\"total\":1}",
                r1.readEntity(String.class));

        assertEquals("yyy", stringForQuery("SELECT name FROM utest.e3 WHERE e2_id = 24"));
    }

    @Test
    public void testRelate_ValidRel_ToOne_Existing_WithUpdate() {

        performQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (24, 'xxx')"));
        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (7, 'zzz')"));
        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (8, 'yyy')"));

        Response r1 = target("/e3/8/e2/24").request()
                .put(Entity.entity("{\"name\":\"123\"}", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":24,\"address\":null,\"name\":\"123\"}],\"total\":1}",
                r1.readEntity(String.class));

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e2"));

        assertEquals("yyy", stringForQuery("SELECT name FROM utest.e3 WHERE e2_id = 24"));
    }

    @Test
    public void testRelate_ToMany_MixedCollection() {

        performQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e8 (id, name) values (15, 'xxx')"));
        performQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e8 (id, name) values (16, 'xxx')"));

        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e7 (id, name, e8_id) values (7, 'zzz', 16)"));
        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e7 (id, name, e8_id) values (8, 'yyy', 15)"));
        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e7 (id, name, e8_id) values (9, 'aaa', 15)"));

        Response r1 = target("/e8/createorupdate/15/e7s").request().put(Entity.entity(
                "[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[" + "{\"id\":1,\"name\":\"newname\"},{\"id\":8,\"name\":\"123\"}],\"total\":2}",
                r1.readEntity(String.class));
        assertEquals(4, intForQuery("SELECT count(1) FROM utest.e7"));

        // testing idempotency

        Response r2 = target("/e8/createorupdate/15/e7s").request().put(Entity.entity(
                "[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r2.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"name\":\"newname\"}," + "{\"id\":8,\"name\":\"123\"}],\"total\":2}",
                r2.readEntity(String.class));
        assertEquals(4, intForQuery("SELECT count(1) FROM utest.e7"));
    }

    @Test
    public void test_ToMany_CreateUpdateDelete() {

        performQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e8 (id, name) values (15, 'xxx')"));
        performQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e8 (id, name) values (16, 'xxx')"));

        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e7 (id, name, e8_id) values (7, 'zzz', 16)"));
        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e7 (id, name, e8_id) values (8, 'yyy', 15)"));
        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e7 (id, name, e8_id) values (9, 'aaa', 15)"));

        // this must add E7 with id=1, update - with id=8, delete - with id=9
        Response r1 = target("/e8/15/e7s").request().put(Entity.entity(
                "[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[" + "{\"id\":1,\"name\":\"newname\"},{\"id\":8,\"name\":\"123\"}],\"total\":2}",
                r1.readEntity(String.class));
        assertEquals(2, intForQuery("SELECT count(1) FROM utest.e7 WHERE e8_id = 15"));
        assertEquals(0, intForQuery("SELECT count(1) FROM utest.e7 WHERE id = 9"));

        // testing idempotency

        Response r2 = target("/e8/15/e7s").request().put(Entity.entity(
                "[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r2.getStatus());
        assertEquals("{\"data\":[" + "{\"id\":1,\"name\":\"newname\"},{\"id\":8,\"name\":\"123\"}],\"total\":2}",
                r2.readEntity(String.class));
        assertEquals(2, intForQuery("SELECT count(1) FROM utest.e7 WHERE e8_id = 15"));
        assertEquals(0, intForQuery("SELECT count(1) FROM utest.e7 WHERE id = 9"));
    }

    @Test
    public void testRelate_ValidRel_ToOne_New_AutogenId() {

        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (7, 'zzz')"));
        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (8, 'yyy')"));

        Response r1 = target("/e3/8/e2/24").request()
                .put(Entity.entity("{\"name\":\"123\"}", MediaType.APPLICATION_JSON));

        assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Can't create 'E2' with fixed id\"}",
                r1.readEntity(String.class));

        assertEquals(0, intForQuery("SELECT count(1) FROM utest.e2"));
    }

    @Test
    public void testRelate_ValidRel_ToOne_New_DefaultId() {

        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e7 (id) values (7)"));
        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e7 (id) values (8)"));

        Response r1 = target("/e7/8/e8/24").request()
                .put(Entity.entity("{\"name\":\"aaa\"}", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":24,\"name\":\"aaa\"}],\"total\":1}", r1.readEntity(String.class));

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e8"));
        assertEquals("aaa", stringForQuery("SELECT name FROM utest.e8"));
        assertEquals(24, intForQuery("SELECT id FROM utest.e8"));
        assertEquals(24, intForQuery("SELECT e8_id FROM utest.e7 WHERE id = 8"));
        assertEquals(-1, intForQuery("SELECT e8_id FROM utest.e7 WHERE id = 7"));

        // PUT is idempotent... doing another update should not change the
        // picture
        Response r2 = target("/e7/8/e8/24").request()
                .put(Entity.entity("{\"name\":\"aaa\"}", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r2.getStatus());
        assertEquals("{\"data\":[{\"id\":24,\"name\":\"aaa\"}],\"total\":1}", r2.readEntity(String.class));

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e8"));
        assertEquals("aaa", stringForQuery("SELECT name FROM utest.e8"));
        assertEquals(24, intForQuery("SELECT id FROM utest.e8"));
        assertEquals(24, intForQuery("SELECT e8_id FROM utest.e7 WHERE id = 8"));
        assertEquals(-1, intForQuery("SELECT e8_id FROM utest.e7 WHERE id = 7"));
    }

    @Test
    public void testRelate_ValidRel_ToOne_New_PropagatedId() {

        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e8 (id) values (7)"));
        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e8 (id) values (8)"));

        Response r1 = target("/e8/8/e9").request().put(Entity.entity("{}", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":8}],\"total\":1}", r1.readEntity(String.class));

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e9"));
        assertEquals(8, intForQuery("SELECT e8_id FROM utest.e9"));

        // PUT is idempotent... doing another update should not change the
        // picture
        Response r2 = target("/e8/8/e9").request().put(Entity.entity("{}", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r2.getStatus());
        assertEquals("{\"data\":[{\"id\":8}],\"total\":1}", r2.readEntity(String.class));

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e9"));
        assertEquals(8, intForQuery("SELECT e8_id FROM utest.e9"));
    }

    @Test
    public void testRelate_ToMany_NoIds() {

        performQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (15, 'xxx')"));
        performQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (16, 'xxx')"));

        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name, e2_id) values (7, 'zzz', 16)"));
        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name, e2_id) values (8, 'yyy', 15)"));
        performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name, e2_id) values (9, 'aaa', 15)"));

        assertEquals(3, intForQuery("SELECT count(1) FROM utest.e3"));
        assertEquals(2, intForQuery("SELECT count(1) FROM utest.e3 WHERE e2_id = 15"));

        // we can't PUT an object with generated ID , as the request is
        // non-repeatable
        Response r1 = target("/e2/15/e3s").request()
                .put(Entity.entity("[ {\"name\":\"newname\"} ]", MediaType.APPLICATION_JSON));

        assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Request is not idempotent.\"}", r1.readEntity(String.class));
        assertEquals(3, intForQuery("SELECT count(1) FROM utest.e3"));
        assertEquals(2, intForQuery("SELECT count(1) FROM utest.e3 WHERE e2_id = 15"));
    }

    @Test
    public void testPUT_ToMany_Join() {

        performQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e12 (id) values (11)"));
        performQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e12 (id) values (12)"));

        performQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e13 (id) values (14)"));
        performQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e13 (id) values (15)"));
        performQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e13 (id) values (16)"));

        Response r1 = target("/e12/12/e1213").queryParam("exclude", "id").request()
                .put(Entity.entity("[{\"e13\":15},{\"e13\":14}]", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{},{}],\"total\":2}", r1.readEntity(String.class));

        assertEquals(2, intForQuery("SELECT count(1) FROM utest.e12_e13"));
        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e12_e13 " + "WHERE e12_id = 12 AND e13_id = 14"));
        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e12_e13 " + "WHERE e12_id = 12 AND e13_id = 15"));

        // testing idempotency
        Response r2 = target("/e12/12/e1213").queryParam("exclude", "id").request()
                .put(Entity.entity("[{\"e13\":15},{\"e13\":14}]", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r2.getStatus());
        assertEquals("{\"data\":[{},{}],\"total\":2}", r2.readEntity(String.class));

        assertEquals(2, intForQuery("SELECT count(1) FROM utest.e12_e13"));
        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e12_e13 WHERE e12_id = 12 AND e13_id = 14"));
        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e12_e13 WHERE e12_id = 12 AND e13_id = 15"));

        // add one and delete another record
        Response r3 = target("/e12/12/e1213").queryParam("exclude", "id").request()
                .put(Entity.entity("[{\"e13\":16},{\"e13\":14}]", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r3.getStatus());
        assertEquals("{\"data\":[{},{}],\"total\":2}", r3.readEntity(String.class));

        assertEquals(2, intForQuery("SELECT count(1) FROM utest.e12_e13"));
        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e12_e13 WHERE e12_id = 12 AND e13_id = 14"));
        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e12_e13 WHERE e12_id = 12 AND e13_id = 16"));
    }

    @Test
    public void testPUT_ToMany_DifferentIdTypes() {

        performQuery(new SQLTemplate(E1.class, "INSERT INTO utest.e1 (id, name) values (1, 'xxx')"));
        performQuery(new SQLTemplate(E1.class, "INSERT INTO utest.e1 (id, name) values (2, 'yyy')"));

        performQuery(new SQLTemplate(E15.class, "INSERT INTO utest.e15 (long_id, name) values (14, 'aaa')"));
        performQuery(new SQLTemplate(E15.class, "INSERT INTO utest.e15 (long_id, name) values (15, 'bbb')"));
        performQuery(new SQLTemplate(E15.class, "INSERT INTO utest.e15 (long_id, name) values (16, 'ccc')"));

        performQuery(new SQLTemplate(E15E1.class, "INSERT INTO utest.e15_e1 (e15_id, e1_id) values (14, 1)"));

        Response r1 = target("/e15/14/e15e1").queryParam("exclude", "id").request()
                .put(Entity.entity("[{\"e1\":1}]", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e15_e1"));
        assertEquals(2, intForQuery("SELECT count(1) FROM utest.e1"));
        assertEquals(3, intForQuery("SELECT count(1) FROM utest.e15"));

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e15_e1 WHERE e15_id = 14 AND e1_id = 1"));
    }

    @Test
    public void testPUT_ToMany_Flattened_DifferentIdTypes() {

        insert("e5", "id, name", "1, 'xxx'");
        insert("e5", "id, name", "2, 'yyy'");

        performQuery(new SQLTemplate(E15.class, "INSERT INTO utest.e15 (long_id, name) values (14, 'aaa')"));
        performQuery(new SQLTemplate(E15.class, "INSERT INTO utest.e15 (long_id, name) values (15, 'bbb')"));
        performQuery(new SQLTemplate(E15.class, "INSERT INTO utest.e15 (long_id, name) values (16, 'ccc')"));

        performQuery(new SQLTemplate(E15E1.class, "INSERT INTO utest.e15_e5 (e15_id, e5_id) values (14, 1)"));

        Response r1 = target("/e15/14").request().put(Entity.entity("{\"e5s\":[1]}", MediaType.APPLICATION_JSON));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e15_e5"));
        assertEquals(2, intForQuery("SELECT count(1) FROM utest.e5"));
        assertEquals(3, intForQuery("SELECT count(1) FROM utest.e15"));

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e15_e5 WHERE e15_id = 14 AND e5_id = 1"));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e2/{id}/e3s")
        public DataResponse<E3> createOrUpdate_Idempotent_E3s(@PathParam("id") int id, String entityData) {
            return LinkRest.idempotentCreateOrUpdate(E3.class, config).toManyParent(E2.class, id, E2.E3S)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e3/{id}/e2/{tid}")
        public DataResponse<E2> createOrUpdate_Idempotent_E2ViaE3(
                @PathParam("id") int parentId,
                @PathParam("tid") int id,
                String entityData) {
            return LinkRest.idempotentCreateOrUpdate(E2.class, config).id(id).parent(E3.class, parentId, E3.E2)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e7/{id}/e8/{tid}")
        public DataResponse<E8> relateToOneExisting(@PathParam("id") int parentId, @PathParam("tid") int id, String data) {
            return LinkRest.idempotentCreateOrUpdate(E8.class, config).id(id).parent(E7.class, parentId, E7.E8)
                    .syncAndSelect(data);
        }

        @PUT
        @Path("e8/{id}/e9")
        public DataResponse<E9> relateToOneDependent(@PathParam("id") int id, String entityData) {
            // this will test support for ID propagation in a 1..1
            return LinkRest.idempotentCreateOrUpdate(E9.class, config).parent(E8.class, id, E8.E9)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e8/createorupdate/{id}/e7s")
        public DataResponse<E7> createOrUpdateE7s(@PathParam("id") int id, String entityData) {
            return LinkRest.idempotentCreateOrUpdate(E7.class, config).toManyParent(E8.class, id, E8.E7S)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e8/{id}/e7s")
        public DataResponse<E7> fullSyncE7s(@PathParam("id") int id, String entityData) {
            return LinkRest.idempotentFullSync(E7.class, config).toManyParent(E8.class, id, E8.E7S)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e12/{id}/e1213")
        public DataResponse<E12E13> fullSync_Joins(@PathParam("id") int id, @Context UriInfo info, String entityData) {
            return LinkRest.idempotentFullSync(E12E13.class, config).toManyParent(E12.class, id, E12.E1213).uri(info)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e15/{id}/e15e1")
        public DataResponse<E15E1> createOrUpdate_Joins(@PathParam("id") long id, @Context UriInfo info, String entityData) {
            return LinkRest.createOrUpdate(E15E1.class, config).toManyParent(E15.class, id, E15.E15E1).uri(info)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e15/{id}")
        public DataResponse<E15> createOrUpdate_Joins_FlattenedRel(@PathParam("id") long id, @Context UriInfo info, String entityData) {
            return LinkRest.createOrUpdate(E15.class, config).id(id).uri(info).syncAndSelect(entityData);
        }
    }
}
