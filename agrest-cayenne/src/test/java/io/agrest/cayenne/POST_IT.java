package io.agrest.cayenne;


import io.agrest.DataResponse;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E16;
import io.agrest.cayenne.cayenne.main.E17;
import io.agrest.cayenne.cayenne.main.E19;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E31;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class POST_IT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class, E16.class, E17.class, E19.class, E31.class)
            .build();

    @Test
    public void test() {

        tester.target("/e4").post("{\"cVarchar\":\"zzz\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
                        + "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"zzz\"}");

        tester.e4().matcher().assertOneMatch();
        tester.e4().matcher().eq("c_varchar", "zzz").assertOneMatch();

        tester.target("/e4").post("{\"cVarchar\":\"TTTT\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
                        + "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"TTTT\"}");

        tester.e4().matcher().assertMatches(2);
        tester.e4().matcher().eq("c_varchar", "TTTT").assertOneMatch();
    }

    @Test
    public void testIdCalledId() {

        tester.target("/e31").post("{\"id\":5,\"name\":\"31\"}")
                .wasCreated()
                .bodyEquals(1, "{\"id\":5,\"name\":\"31\"}");

        tester.e31().matcher().assertOneMatch();
        tester.e31().matcher().eq("id", 5L).assertOneMatch();
    }

    @Test
    public void testCompoundId() {

        tester.target("/e17")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .post("{\"name\":\"xxx\"}")
                .wasCreated()
                .bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"xxx\"}");
    }

    @Test
    public void testDateTime() {
        tester.target("e16")
                .post("{\"cDate\":\"2015-03-14\", \"cTime\":\"T19:00:00\", \"cTimestamp\":\"2015-03-14T19:00:00.000\"}")
                .wasCreated()
                // TODO: why is time returned back without a "T" prefix?
                .bodyEquals(1, "{\"id\":1,\"cDate\":\"2015-03-14\",\"cTime\":\"19:00:00\",\"cTimestamp\":\"2015-03-14T19:00:00\"}");
    }

    @Test
    public void testSync_NoData() {

        tester.target("/e4/sync")
                .post("{\"cVarchar\":\"zzz\"}")
                .wasCreated()
                .bodyEquals("{}");

        tester.e4().matcher().assertOneMatch();
        tester.e4().matcher().eq("c_varchar", "zzz").assertOneMatch();
    }

    @Test
    public void testToOne() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.target("/e3")
                .post("{\"e2\":8,\"name\":\"MM\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"MM\",\"phoneNumber\":null}");

        tester.e3().matcher().assertOneMatch();
        tester.e3().matcher().eq("e2_id", 8).eq("name", "MM").assertOneMatch();
    }

    @Test
    public void testToOne_Null() {

        tester.target("/e3")
                .post("{\"e2_id\":null,\"name\":\"MM\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"MM\",\"phoneNumber\":null}");

        tester.e3().matcher().assertOneMatch();
        tester.e3().matcher().eq("e2_id", null).assertOneMatch();
    }

    @Test
    public void testToOne_BadFK() {

        tester.target("/e3")
                .post("{\"e2\":15,\"name\":\"MM\"}")
                .wasNotFound()
                .bodyEquals("{\"message\":\"Related object 'E2' with ID '[15]' is not found\"}");

        tester.e3().matcher().assertNoMatches();
    }

    @Test
    public void testBulk() {

        tester.target("/e3/")
                .queryParam("exclude", "id")
                .queryParam("include", E3.NAME.getName())
                .post("[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"},{\"name\":\"yyy\"}]")
                .wasCreated()
                // ordering from request must be preserved...
                .bodyEquals(4, "{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"},{\"name\":\"yyy\"}");
    }

    @Test
    public void testToMany() {

        tester.e3().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        Long id = tester.target("/e2")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .post("{\"e3s\":[1,8],\"name\":\"MM\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"e3s\":[{\"id\":1},{\"id\":8}],\"name\":\"MM\"}")
                .getId();

        assertNotNull(id);

        tester.e3().matcher().eq("e2_id", id).assertMatches(2);
    }

    @Test
    public void testByteArrayProperty() {

        String base64Encoded = "c29tZVZhbHVlMTIz"; // someValue123

        tester.target("/e19")
                .queryParam("include", E19.GUID.getName())
                .post("{\"guid\":\"" + base64Encoded + "\"}")
                .wasCreated()
                .bodyEquals(1, "{\"guid\":\"" + base64Encoded + "\"}");
    }

    @Test
    public void testFloatProperty() {
        tester.target("/e19/float")
                .queryParam("include", "floatObject", "floatPrimitive")
                .post("{\"floatObject\":1.0,\"floatPrimitive\":2.0}")
                .wasCreated()
                .bodyEquals(1, "{\"floatObject\":1.0,\"floatPrimitive\":2.0}");
        tester.e19().matcher().eq("float_object", 1.0).eq("float_primitive", 2.0).assertOneMatch();
    }

    @Test
    public void testFloatProperty_FromInt() {
        tester.target("/e19/float")
                .queryParam("include", "floatObject", "floatPrimitive")
                .post("{\"floatObject\":1,\"floatPrimitive\":2}")
                .wasCreated()
                .bodyEquals(1, "{\"floatObject\":1.0,\"floatPrimitive\":2.0}");
        tester.e19().matcher().eq("float_object", 1.0).eq("float_primitive", 2.0).assertOneMatch();
    }

    @Test
    public void testDoubleProperty() {
        tester.target("/e19/double").post("{\"doubleObject\":1.0,\"doublePrimitive\":2.0}").wasCreated();
        tester.e19().matcher().eq("double_object", 1.0).eq("double_primitive", 2.0).assertOneMatch();
    }

    @Test
    public void testDoubleProperty_FromInt() {
        tester.target("/e19/double").post("{\"doubleObject\":1,\"doublePrimitive\":2}").wasCreated();
        tester.e19().matcher().eq("double_object", 1.0).eq("double_primitive", 2.0).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e2")
        public DataResponse<E2> createE2(String targetData, @Context UriInfo uriInfo) {
            return AgJaxrs.create(E2.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(targetData);
        }

        @POST
        @Path("e3")
        public DataResponse<E3> create(@Context UriInfo uriInfo, String requestBody) {
            return AgJaxrs.create(E3.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(requestBody);
        }

        @POST
        @Path("e4")
        public DataResponse<E4> createE4(String requestBody) {
            return AgJaxrs.create(E4.class, config).syncAndSelect(requestBody);
        }

        @POST
        @Path("e4/sync")
        public SimpleResponse createE4_DefaultData(String requestBody) {
            return AgJaxrs.create(E4.class, config).sync(requestBody);
        }

        @POST
        @Path("e16")
        public DataResponse<E16> createE16(String requestBody) {
            return AgJaxrs.create(E16.class, config).syncAndSelect(requestBody);
        }

        @POST
        @Path("e17")
        public DataResponse<E17> createE17(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2,
                String requestBody) {

            Map<String, Object> ids = new HashMap<>();
            ids.put(E17.ID1.getName(), id1);
            ids.put(E17.ID2.getName(), id2);

            return AgJaxrs.create(E17.class, config).byId(ids).syncAndSelect(requestBody);
        }

        @POST
        @Path("e19")
        public DataResponse<E19> createE19(@Context UriInfo uriInfo, String data) {
            return AgJaxrs.create(E19.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(data);
        }

        @POST
        @Path("e19/float")
        public DataResponse<E19> createE19_FloatAttribute(@Context UriInfo uriInfo, String data) {
            return AgJaxrs.create(E19.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(data);
        }

        @POST
        @Path("e19/double")
        public SimpleResponse create_E19_DoubleAttribute(String entityData) {
            return AgJaxrs.create(E19.class, config).sync(entityData);
        }

        @POST
        @Path("e31")
        public DataResponse<E31> createE31(@Context UriInfo uriInfo, String requestBody) {
            return AgJaxrs.create(E31.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(requestBody);
        }
    }
}
