package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.SimpleResponse;
import io.agrest.constraints.Constraint;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E16;
import io.agrest.it.fixture.cayenne.E17;
import io.agrest.it.fixture.cayenne.E19;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.it.fixture.cayenne.E8;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class POST_IT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E4.class, E8.class, E16.class, E17.class, E19.class};
    }

    @Test
    public void test() {

        Response r1 = target("/e4").request().post(Entity.json("{\"cVarchar\":\"zzz\"}"));
        onResponse(r1)
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
                        + "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"zzz\"}");

        e4().matcher().assertOneMatch();
        e4().matcher().eq("c_varchar", "zzz").assertOneMatch();

        Response r2 = target("/e4").request().post(Entity.json("{\"cVarchar\":\"TTTT\"}"));
        onResponse(r2)
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
                        + "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"TTTT\"}");

        e4().matcher().assertMatches(2);
        e4().matcher().eq("c_varchar", "TTTT").assertOneMatch();
    }

    @Test
    public void testCompoundId() {

        Response r = target("/e17")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .request()
                .post(Entity.json("{\"name\":\"xxx\"}"));

        onResponse(r)
                .wasCreated()
                .bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"xxx\"}");
    }

    @Test
    public void testDateTime() {
        Response r = target("e16")
                .request()
                .post(Entity.json("{\"cDate\":\"2015-03-14\", \"cTime\":\"T19:00:00\", \"cTimestamp\":\"2015-03-14T19:00:00.000\"}"));

        onResponse(r)
                .wasCreated()
                // TODO: why is time returned back without a "T" prefix?
                .bodyEquals(1, "{\"id\":1,\"cDate\":\"2015-03-14\",\"cTime\":\"19:00:00\",\"cTimestamp\":\"2015-03-14T19:00:00\"}");
    }

    @Test
    public void testSync_NoData() {

        Response r = target("/e4/sync")
                .request()
                .post(Entity.json("{\"cVarchar\":\"zzz\"}"));

        onResponse(r)
                .wasCreated()
                .bodyEquals("{\"success\":true}");

        e4().matcher().assertOneMatch();
        e4().matcher().eq("c_varchar", "zzz").assertOneMatch();
    }

    @Test
    public void testWriteConstraints_Id_Allowed() {

        // endpoint constraint allows "name" and "id"

        Response r = target("/e8/w/constrainedid/578")
                .request()
                .post(Entity.json("{\"name\":\"zzz\"}"));

        onResponse(r)
                .wasCreated()
                .bodyEquals("{\"success\":true}");

        e8().matcher().assertOneMatch();
        e8().matcher().eq("id", 578).eq("name", "zzz").assertOneMatch();
    }

    @Test
    public void testWriteConstraints_Id_Blocked() {

        // endpoint constraint allows "name", but not "id"

        Response r = target("/e8/w/constrainedidblocked/578")
                .request()
                .post(Entity.json("{\"name\":\"zzz\"}"));

        onResponse(r)
                .statusEquals(Response.Status.BAD_REQUEST)
                .bodyEquals("{\"success\":false,\"message\":\"Setting ID explicitly is not allowed: {id=578}\"}");

        e8().matcher().assertNoMatches();
    }

    @Test
    public void testWriteConstraints1() {

        Response r = target("/e3/w/constrained").request()
                .post(Entity.json("{\"name\":\"zzz\"}"));

        onResponse(r)
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"zzz\",\"phoneNumber\":null}");
    }

    @Test
    public void testWriteConstraints2() {

        Response r = target("/e3/w/constrained").request()
                .post(Entity.json("{\"name\":\"zzz\",\"phoneNumber\":\"12345\"}"));

        // writing phone number is not allowed, so it was ignored
        onResponse(r)
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"zzz\",\"phoneNumber\":null}");

        // TODO: can't use matcher for NULLs until BQ 1.1 upgrade (because of https://github.com/bootique/bootique-jdbc/issues/91 )
        //  so using select...

        e3().matcher().assertOneMatch();
        List<String> phones = e3()
                .selectStatement(rs -> rs.getString(1)).append("SELECT phone_number FROM utest.e3")
                .select(100);
        assertEquals(1, phones.size());
        assertNull(phones.get(0));
    }

    @Test
    public void testReadConstraints1() {

        Response r = target("/e3/constrained").request()
                .post(Entity.json("{\"name\":\"zzz\"}"));

        onResponse(r)
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"zzz\"}");
    }

    @Test
    public void testInclude_ReadConstraints() {

        // writing "phoneNumber" is allowed, but reading is not ... must be in DB, but not in response

        Response r = target("/e3/constrained")
                .queryParam("include", "name")
                .queryParam("include", "phoneNumber")
                .request()
                .post(Entity.json("{\"name\":\"zzz\",\"phoneNumber\":\"123456\"}"));

        onResponse(r)
                .wasCreated()
                .bodyEquals(1, "{\"name\":\"zzz\"}");

        e3().matcher().assertOneMatch();
        e3().matcher().eq("name", "zzz").eq("phone_number", "123456").assertOneMatch();
    }

    @Test
    public void testReadConstraints_DisallowRelated() {

        Response r = target("/e3/constrained")
                .queryParam("include", E3.E2.getName())
                .request()
                .post(Entity.json("{\"name\":\"zzz\"}"));

        onResponse(r)
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"zzz\"}");
    }


    @Test
    public void testToOne() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        Response r = target("/e3")
                .request()
                .post(Entity.json("{\"e2\":8,\"name\":\"MM\"}"));

        onResponse(r)
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"MM\",\"phoneNumber\":null}");

        e3().matcher().assertOneMatch();
        e3().matcher().eq("e2_id", 8).eq("name", "MM").assertOneMatch();
    }

    @Test
    public void testToOne_Null() {

        Response r = target("/e3")
                .request()
                .post(Entity.json("{\"e2_id\":null,\"name\":\"MM\"}"));

        onResponse(r)
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"MM\",\"phoneNumber\":null}");

        e3().matcher().assertOneMatch();

        // TODO: can't use matcher for NULLs until BQ 1.1 upgrade (because of https://github.com/bootique/bootique-jdbc/issues/91 )
        //  so using select...

        e3().matcher().assertOneMatch();
        List<String> fks = e3()
                .selectStatement(rs -> rs.getString(1)).append("SELECT e2_id FROM utest.e3")
                .select(100);
        assertEquals(1, fks.size());
        assertNull(fks.get(0));
    }

    @Test
    public void testToOne_BadFK() {

        Response r = target("/e3")
                .request()
                .post(Entity.json("{\"e2\":15,\"name\":\"MM\"}"));

        onResponse(r)
                .statusEquals(Response.Status.NOT_FOUND)
                .bodyEquals("{\"success\":false,\"message\":\"Related object 'E2' with ID '[15]' is not found\"}");

        e3().matcher().assertNoMatches();
    }

    @Test
    public void testBulk() {

        Response r = target("/e3/")
                .queryParam("exclude", "id")
                .queryParam("include", E3.NAME.getName())
                .request()
                .post(Entity.json("[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"},{\"name\":\"yyy\"}]"));

        // ordering from request must be preserved...
        onResponse(r)
                .wasCreated()
                .bodyEquals(4, "{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"},{\"name\":\"yyy\"}");
    }

    @Test
    public void testToMany() {

        e3().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        Response r = target("/e2")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .request()
                .post(Entity.json("{\"e3s\":[1,8],\"name\":\"MM\"}"));

        Long id = onResponse(r)
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"e3s\":[{\"id\":1},{\"id\":8}],\"name\":\"MM\"}")
                .getId();

        assertNotNull(id);

        e3().matcher().eq("e2_id", id).assertMatches(2);
    }

    @Test
    public void testByteArrayProperty() {

        String base64Encoded = "c29tZVZhbHVlMTIz"; // someValue123

        Response response = target("/e19")
                .queryParam("include", E19.GUID.getName())
                .request()
                .post(Entity.json("{\"guid\":\"" + base64Encoded + "\"}"));

        onResponse(response)
                .wasCreated()
                .bodyEquals(1, "{\"guid\":\"" + base64Encoded + "\"}");
    }

    @Test
    public void testFloatProperty() {
        String data = "{\"floatObject\":0,\"floatPrimitive\":0}";
        Response response = target("/e19").path("float")
                .queryParam("include", "floatObject")
                .queryParam("include", "floatPrimitive")
                .request()
                .post(Entity.json(data));

        onResponse(response)
                .wasCreated()
                .bodyEquals(1, "{\"floatObject\":0.0,\"floatPrimitive\":0.0}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e2")
        public DataResponse<E2> createE2(String targetData, @Context UriInfo uriInfo) {
            return Ag.create(E2.class, config).uri(uriInfo).syncAndSelect(targetData);
        }

        @POST
        @Path("e3")
        public DataResponse<E3> create(@Context UriInfo uriInfo, String requestBody) {
            return Ag.create(E3.class, config).uri(uriInfo).syncAndSelect(requestBody);
        }

        @POST
        @Path("e3/constrained")
        public DataResponse<E3> insertE3ReadConstrained(@Context UriInfo uriInfo, String requestBody) {
            Constraint<E3> tc = Constraint.idOnly(E3.class).attribute(E3.NAME);
            return Ag.create(E3.class, config).uri(uriInfo).readConstraint(tc).syncAndSelect(requestBody);
        }

        @POST
        @Path("e3/w/constrained")
        public DataResponse<E3> insertE3WriteConstrained(@Context UriInfo uriInfo, String requestBody) {
            Constraint<E3> tc = Constraint.idOnly(E3.class).attribute(E3.NAME);
            return Ag.create(E3.class, config).uri(uriInfo).writeConstraint(tc).syncAndSelect(requestBody);
        }

        @POST
        @Path("e4")
        public DataResponse<E4> createE4(String requestBody) {
            return Ag.create(E4.class, config).syncAndSelect(requestBody);
        }

        @POST
        @Path("e4/sync")
        public SimpleResponse createE4_DefaultData(String requestBody) {
            return Ag.create(E4.class, config).sync(requestBody);
        }

        @POST
        @Path("e8/w/constrainedid/{id}")
        public SimpleResponse create_WriteConstrainedId(
                @PathParam("id") int id,
                @Context UriInfo uriInfo,
                String requestBody) {

            Constraint<E8> tc = Constraint.idOnly(E8.class).attribute(E8.NAME);
            return Ag.create(E8.class, config).uri(uriInfo).id(id).writeConstraint(tc).sync(requestBody);
        }

        @POST
        @Path("e8/w/constrainedidblocked/{id}")
        public SimpleResponse create_WriteConstrainedIdBlocked(
                @PathParam("id") int id,
                @Context UriInfo uriInfo,
                String requestBody) {
            Constraint<E8> tc = Constraint.excludeAll(E8.class).attribute(E8.NAME);
            return Ag.create(E8.class, config).uri(uriInfo).id(id).writeConstraint(tc).sync(requestBody);
        }

        @POST
        @Path("e16")
        public DataResponse<E16> createE16(String requestBody) {
            return Ag.create(E16.class, config).syncAndSelect(requestBody);
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

            return Ag.create(E17.class, config).id(ids).syncAndSelect(requestBody);
        }

        @POST
        @Path("e19")
        public DataResponse<E19> createE19(@Context UriInfo uriInfo, String data) {
            return Ag.create(E19.class, config).uri(uriInfo).syncAndSelect(data);
        }

        @POST
        @Path("e19/float")
        public DataResponse<E19> createE19_FloatAttribute(@Context UriInfo uriInfo, String data) {
            DataResponse<E19> response = Ag.create(E19.class, config).uri(uriInfo).syncAndSelect(data);

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
    }
}
