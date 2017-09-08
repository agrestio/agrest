package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E16;
import com.nhl.link.rest.it.fixture.cayenne.E17;
import com.nhl.link.rest.it.fixture.cayenne.E19;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.cayenne.E8;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SQLSelect;
import org.apache.cayenne.query.SelectQuery;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nhl.link.rest.unit.matcher.LRMatchers.hasStatusAndBody;
import static org.junit.Assert.*;

public class POST_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testPost() {

        ObjectContext context = newContext();

        Response response1 = target("/e4").request()
                .post(Entity.entity("{\"cVarchar\":\"zzz\"}", MediaType.APPLICATION_JSON));
        assertEquals(Status.CREATED.getStatusCode(), response1.getStatus());

        E4 e41 = ObjectSelect.query(E4.class).selectFirst(context);
        assertNotNull(e41);
        assertEquals("zzz", e41.getCVarchar());
        int id1 = Cayenne.intPKForObject(e41);

        assertEquals(
                "{\"data\":[{\"id\":" + id1 + ",\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
                        + "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"zzz\"}],\"total\":1}",
                response1.readEntity(String.class));

        Response response2 = target("/e4").request()
                .post(Entity.entity("{\"cVarchar\":\"TTTT\"}", MediaType.APPLICATION_JSON));

        assertEquals(Status.CREATED.getStatusCode(), response2.getStatus());

        List<E4> e4s = context.select(new SelectQuery<E4>(E4.class));
        assertEquals(2, e4s.size());
        assertTrue(e4s.remove(e41));

        E4 e42 = e4s.get(0);
        assertEquals("TTTT", e42.getCVarchar());
        int id2 = Cayenne.intPKForObject(e42);

        assertEquals(
                "{\"data\":[{\"id\":" + id2 + ",\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
                        + "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"TTTT\"}],\"total\":1}",
                response2.readEntity(String.class));
    }

    @Test
    public void testPost_ExplicitCompoundId() {

        Response response1 = target("/e17").queryParam("id1", 1).queryParam("id2", 1).request()
                .post(Entity.entity("{\"name\":\"xxx\"}", MediaType.APPLICATION_JSON));
        assertEquals(Status.CREATED.getStatusCode(), response1.getStatus());
    }

    @Test
    public void testPost_DateTime() {
        Response r1 = target("e16").request()
                .post(Entity.entity(
                        "{\"cDate\":\"2015-03-14\", \"cTime\":\"T19:00:00\", \"cTimestamp\":\"2015-03-14T19:00:00.000\"}",
                        MediaType.APPLICATION_JSON));
        assertEquals(Status.CREATED.getStatusCode(), r1.getStatus());
    }

    @Test
    public void testPost_Default_NoData() {
        ObjectContext context = newContext();

        Response response1 = target("/e4/defaultdata").request()
                .post(Entity.entity("{\"cVarchar\":\"zzz\"}", MediaType.APPLICATION_JSON));
        assertEquals(Status.CREATED.getStatusCode(), response1.getStatus());
        assertEquals("{\"success\":true}", response1.readEntity(String.class));

        E4 e41 = (E4) Cayenne.objectForQuery(context, new SelectQuery<E4>(E4.class));
        assertEquals("zzz", e41.getCVarchar());
    }

    @Test
    public void testPost_WriteConstraints_Id_Allowed() {
        ObjectContext context = newContext();

        Response r1 = target("/e8/w/constrainedid/578").request()
                .post(Entity.entity("{\"name\":\"zzz\"}", MediaType.APPLICATION_JSON));
        assertEquals(Status.CREATED.getStatusCode(), r1.getStatus());

        assertEquals(Integer.valueOf(1),
                SQLSelect.scalarQuery(Integer.class, "SELECT count(1) FROM utest.e8").selectOne(context));
        assertEquals("zzz", SQLSelect.scalarQuery(String.class, "SELECT name FROM utest.e8").selectOne(context));
        assertEquals(578, intForQuery("SELECT id FROM utest.e8"));
    }

    @Test
    public void testPost_WriteConstraints_Id_Blocked() {

        Response r1 = target("/e8/w/constrainedidblocked/578").request()
                .post(Entity.entity("{\"name\":\"zzz\"}", MediaType.APPLICATION_JSON));
        assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());

        assertEquals(0, intForQuery("SELECT count(1) FROM utest.e8"));
    }

    @Test
    public void testPost_WriteConstraints1() {

        Response r1 = target("/e3/w/constrained").request()
                .post(Entity.entity("{\"name\":\"zzz\"}", MediaType.APPLICATION_JSON));
        assertEquals(Status.CREATED.getStatusCode(), r1.getStatus());

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e3"));
        assertEquals("zzz", stringForQuery("SELECT name FROM utest.e3"));
        int id1 = intForQuery("SELECT id FROM utest.e3");

        assertEquals("{\"data\":[{\"id\":" + id1 + ",\"name\":\"zzz\",\"phoneNumber\":null}],\"total\":1}",
                r1.readEntity(String.class));
    }

    @Test
    public void testPost_WriteConstraints2() {

        Response r2 = target("/e3/w/constrained").request()
                .post(Entity.entity("{\"name\":\"yyy\",\"phoneNumber\":\"12345\"}", MediaType.APPLICATION_JSON));
        assertEquals(Status.CREATED.getStatusCode(), r2.getStatus());

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e3 WHERE name = 'yyy'"));
        int id1 = intForQuery("SELECT id FROM utest.e3  WHERE name = 'yyy'");

        assertEquals("{\"data\":[{\"id\":" + id1 + ",\"name\":\"yyy\",\"phoneNumber\":null}],\"total\":1}",
                r2.readEntity(String.class));
    }

    @Test
    public void testPost_ReadConstraints1() {

        Response r1 = target("/e3/constrained").request()
                .post(Entity.entity("{\"name\":\"zzz\"}", MediaType.APPLICATION_JSON));
        assertEquals(Status.CREATED.getStatusCode(), r1.getStatus());

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e3"));
        assertEquals("zzz", stringForQuery("SELECT name FROM utest.e3"));
        int id1 = intForQuery("SELECT id FROM utest.e3");

        assertEquals("{\"data\":[{\"id\":" + id1 + ",\"name\":\"zzz\"}],\"total\":1}", r1.readEntity(String.class));
    }

    @Test
    public void testPost_ReadConstraints2() {

        Response r2 = target("/e3/constrained").queryParam("include", "name").queryParam("include", "phoneNumber")
                .request().post(Entity.entity("{\"name\":\"yyy\"}", MediaType.APPLICATION_JSON));
        assertEquals(Status.CREATED.getStatusCode(), r2.getStatus());

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e3 WHERE name = 'yyy'"));

        assertEquals("{\"data\":[{\"name\":\"yyy\"}],\"total\":1}", r2.readEntity(String.class));
    }

    @Test
    public void testPost_ReadConstraints3() {

        Response r2 = target("/e3/constrained").queryParam("include", E3.E2.getName()).request()
                .post(Entity.entity("{\"name\":\"yyy\"}", MediaType.APPLICATION_JSON));
        assertEquals(Status.CREATED.getStatusCode(), r2.getStatus());

        assertEquals(1, intForQuery("SELECT count(1) FROM utest.e3 WHERE name = 'yyy'"));
        int id2 = intForQuery("SELECT id FROM utest.e3 WHERE name = 'yyy'");

        assertEquals("{\"data\":[{\"id\":" + id2 + ",\"name\":\"yyy\"}],\"total\":1}", r2.readEntity(String.class));
    }

    @Test
    public void testPost_ToOne() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");

        Response response1 = target("/e3").request()
                .post(Entity.entity("{\"e2\":8,\"name\":\"MM\"}", MediaType.APPLICATION_JSON));

        assertEquals(Status.CREATED.getStatusCode(), response1.getStatus());

        E3 e3 = (E3) Cayenne.objectForQuery(newContext(), new SelectQuery<E3>(E3.class));
        int id = Cayenne.intPKForObject(e3);

        assertEquals("{\"data\":[{\"id\":" + id + ",\"name\":\"MM\",\"phoneNumber\":null}],\"total\":1}",
                response1.readEntity(String.class));

        newContext().invalidateObjects(e3);
        assertEquals("MM", e3.getName());
        assertEquals(8, Cayenne.intPKForObject(e3.getE2()));
    }

    @Test
    public void testPost_ToOne_Null() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");

        Response response1 = target("/e3").request()
                .post(Entity.entity("{\"e2_id\":null,\"name\":\"MM\"}", MediaType.APPLICATION_JSON));

        assertEquals(Status.CREATED.getStatusCode(), response1.getStatus());

        E3 e3 = (E3) Cayenne.objectForQuery(newContext(), new SelectQuery<E3>(E3.class));
        int id = Cayenne.intPKForObject(e3);

        assertEquals("{\"data\":[{\"id\":" + id + ",\"name\":\"MM\",\"phoneNumber\":null}],\"total\":1}",
                response1.readEntity(String.class));

        newContext().invalidateObjects(e3);
        assertEquals("MM", e3.getName());
        assertNull(e3.getE2());
    }

    @Test
    public void testPost_ToOne_BadFK() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");

        Response response1 = target("/e3").request()
                .post(Entity.entity("{\"e2\":15,\"name\":\"MM\"}", MediaType.APPLICATION_JSON));

        assertEquals(Status.NOT_FOUND.getStatusCode(), response1.getStatus());

        assertEquals(0, newContext().select(new SelectQuery<E3>(E3.class)).size());
    }

    @Test
    public void testPost_Bulk() {

        Response r2 = target("/e3/").queryParam("exclude", "id").queryParam("include", E3.NAME.getName()).request()
                .post(Entity.entity("[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"},{\"name\":\"yyy\"}]",
                        MediaType.APPLICATION_JSON));
        assertEquals(Status.CREATED.getStatusCode(), r2.getStatus());

        // ordering must be preserved...
        assertEquals(
                "{\"data\":[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"},{\"name\":\"yyy\"}],\"total\":4}",
                r2.readEntity(String.class));
    }

    @Test
    public void testPost_ToMany() {

        insert("e3", "id, name", "1, 'xxx'");
        insert("e3", "id, name", "8, 'yyy'");

        Response response = target("/e2").queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.E3S.dot(E3.NAME).getName(),
                        E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .request().post(Entity.entity("{\"e3s\":[1,8],\"name\":\"MM\"}", MediaType.APPLICATION_JSON));

        E2 e2 = (E2) Cayenne.objectForQuery(newContext(), new SelectQuery<>(E2.class));
        int id = Cayenne.intPKForObject(e2);

        assertThat(response, hasStatusAndBody(Status.CREATED,
                "{\"data\":[{\"id\":" + id + ",\"e3s\":[{\"id\":1},{\"id\":8}],\"name\":\"MM\"}],\"total\":1}"));
        assertEquals(2, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE e2_id = " + id));
    }

    @Test
    public void testPost_ByteArrayProperty() {

        String base64Encoded = "c29tZVZhbHVlMTIz"; // someValue123

        Response response = target("/e19").queryParam("include", E19.GUID.getName()).request()
                .post(jsonEntity("{\"guid\":\"" + base64Encoded + "\"}"));

        assertThat(response,
                hasStatusAndBody(Status.CREATED, "{\"data\":[{\"guid\":\"" + base64Encoded + "\"}],\"total\":1}"));
    }

    @Test
    public void testPost_FloatProperty() {
        String data = "{\"floatObject\":0,\"floatPrimitive\":0}";
        Response response = target("/e19").path("float")
                .queryParam("include", "floatObject")
                .queryParam("include", "floatPrimitive")
                .request()
                .post(jsonEntity(data));

        assertThat(response, hasStatusAndBody(Status.CREATED,
                "{\"data\":[{\"floatObject\":0.0,\"floatPrimitive\":0.0}],\"total\":1}"));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e2")
        public DataResponse<E2> createE2(String targetData, @Context UriInfo uriInfo) {
            return LinkRest.create(E2.class, config).uri(uriInfo).syncAndSelect(targetData);
        }

        @POST
        @Path("e3")
        public DataResponse<E3> create(@Context UriInfo uriInfo, String requestBody) {
            return LinkRest.create(E3.class, config).uri(uriInfo).syncAndSelect(requestBody);
        }

        @POST
        @Path("e3/constrained")
        public DataResponse<E3> insertE3ReadConstrained(@Context UriInfo uriInfo, String requestBody) {
            Constraint<E3> tc = Constraint.idOnly(E3.class).attribute(E3.NAME);
            return LinkRest.create(E3.class, config).uri(uriInfo).readConstraint(tc).syncAndSelect(requestBody);
        }

        @POST
        @Path("e3/w/constrained")
        public DataResponse<E3> insertE3WriteConstrained(@Context UriInfo uriInfo, String requestBody) {
            Constraint<E3> tc = Constraint.idOnly(E3.class).attribute(E3.NAME);
            return LinkRest.create(E3.class, config).uri(uriInfo).writeConstraint(tc).syncAndSelect(requestBody);
        }

        @POST
        @Path("e4")
        public DataResponse<E4> createE4(String requestBody) {
            return LinkRest.create(E4.class, config).syncAndSelect(requestBody);
        }

        @POST
        @Path("e4/defaultdata")
        public SimpleResponse createE4_DefaultData(String requestBody) {
            return LinkRest.create(E4.class, config).sync(requestBody);
        }

        @POST
        @Path("e8/w/constrainedid/{id}")
        public SimpleResponse create_WriteConstrainedId(
                @PathParam("id") int id,
                @Context UriInfo uriInfo,
                String requestBody) {

            Constraint<E8> tc = Constraint.idOnly(E8.class).attribute(E8.NAME);
            return LinkRest.create(E8.class, config).uri(uriInfo).id(id).writeConstraints(tc).sync(requestBody);
        }

        @POST
        @Path("e8/w/constrainedidblocked/{id}")
        public DataResponse<E8> create_WriteConstrainedIdBlocked(
                @PathParam("id") int id,
                @Context UriInfo uriInfo,
                String requestBody) {
            Constraint<E8> tc = Constraint.excludeAll(E8.class).attribute(E8.NAME);
            return LinkRest.create(E8.class, config).uri(uriInfo).id(id).writeConstraints(tc).syncAndSelect(requestBody);
        }

        @POST
        @Path("e19")
        public DataResponse<E19> createE19(@Context UriInfo uriInfo, String data) {
            return LinkRest.create(E19.class, config).uri(uriInfo).syncAndSelect(data);
        }

        @POST
        @Path("e19/float")
        public DataResponse<E19> createE19_FloatAttribute(@Context UriInfo uriInfo, String data) {
            DataResponse<E19> response = LinkRest.create(E19.class, config).uri(uriInfo).syncAndSelect(data);

            int objectCount = response.getObjects().size();
            if (objectCount > 1) {
                throw new IllegalStateException("unexpected number of objects: " + objectCount);
            }
            E19 e19 = response.getObjects().get(0);
            // trigger type casts
            e19.getFloatObject();
            e19.getFloatPrimitive();

            return response;
        }

        @POST
        @Path("e17")
        public DataResponse<E17> createE17(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2,
                String requestBody) {

            Map<String, Object> ids = new HashMap<>();
            ids.put(E17.ID1_PK_COLUMN, id1);
            ids.put(E17.ID2_PK_COLUMN, id2);

            return LinkRest.create(E17.class, config).id(ids).syncAndSelect(requestBody);
        }

        @POST
        @Path("e16")
        public DataResponse<E16> createE16(String requestBody) {
            return LinkRest.create(E16.class, config).syncAndSelect(requestBody);
        }
    }
}
