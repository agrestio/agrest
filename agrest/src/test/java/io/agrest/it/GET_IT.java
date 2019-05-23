package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.encoder.DateTimeFormatters;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E17;
import io.agrest.it.fixture.cayenne.E19;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.it.fixture.cayenne.E6;
import io.agrest.parser.converter.UtcDateConverter;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GET_IT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E4.class, E6.class, E17.class, E19.class};
    }

    @Test
    public void testResponse() {

        e4().insertColumns("id", "c_varchar", "c_int").values(1, "xxx", 5).exec();

        Response response = target("/e4").request().get();
        onSuccess(response).bodyEquals(1,
                "{\"id\":1,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,"
                        + "\"cInt\":5,\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"xxx\"}");
    }

    @Test
    public void testDateTime() {

        Date date = Date.from(Instant.from(UtcDateConverter.dateParser().fromString("2012-02-03T11:01:02Z")));
        e4().insertColumns("c_timestamp").values(date).exec();

        String dateString = DateTimeFormatters.isoLocalDateTime().format(Instant.ofEpochMilli(date.getTime()));

        Response response = target("/e4").queryParam("include", E4.C_TIMESTAMP.getName()).request().get();
        onSuccess(response).bodyEquals(1, "{\"cTimestamp\":\"" + dateString + "\"}");
    }

    @Test
    public void testDate() {

        Date date = Date.from(Instant.from(UtcDateConverter.dateParser().fromString("2012-02-03")));
        e4().insertColumns("c_date").values(date).exec();

        String dateString = DateTimeFormatters.isoLocalDateTime().format(Instant.ofEpochMilli(date.getTime()));

        Response response = target("/e4").queryParam("include", E4.C_DATE.getName()).request().get();
        onSuccess(response).bodyEquals(1, "{\"cDate\":\"" + dateString + "\"}");
    }

    @Test
    public void testTime() {

        LocalTime lt = LocalTime.of(14, 0, 1);

        // "14:00:01"
        Time time = Time.valueOf(lt);

        e4().insertColumns("c_time").values(time).exec();

        String timeString = DateTimeFormatters.isoLocalDateTime().format(Instant.ofEpochMilli(time.getTime()));

        Response response = target("/e4").queryParam("include", E4.C_TIME.getName()).request().get();
        onSuccess(response).bodyEquals(1, "{\"cTime\":\"" + timeString + "\"}");
    }

    // TODO: add tests for java.sql attributes

    @Test
    public void testSort_ById() {

        e4().insertColumns("id")
                .values(2)
                .values(1)
                .values(3).exec();

        Response response = target("/e4")
                .queryParam("sort", urlEnc("[{\"property\":\"id\",\"direction\":\"DESC\"}]"))
                .queryParam("include", "id")
                .request()
                .get();

        onSuccess(response).bodyEquals(3, "{\"id\":3}", "{\"id\":2}", "{\"id\":1}");
    }

    @Test
    public void testSort_Invalid() {

        Response response = target("/e4")
                .queryParam("sort", urlEnc("[{\"property\":\"xyz\",\"direction\":\"DESC\"}]"))
                .queryParam("include", "id")
                .request()
                .get();

        onResponse(response)
                .statusEquals(Response.Status.BAD_REQUEST)
                .bodyEquals("{\"success\":false,\"message\":\"Invalid path 'xyz' for 'E4'\"}");
    }

    @Test
    // this is a hack for Sencha bug, passing us null sorters per LF-189...
    // allowing for lax property name checking as a result
    public void testSort_Null() {

        e4().insertColumns("id")
                .values(2)
                .values(1)
                .values(3).exec();

        Response response = target("/e4")
                .queryParam("sort", urlEnc("[{\"property\":null,\"direction\":\"DESC\"}]"))
                .queryParam("include", "id")
                .request()
                .get();

        onSuccess(response).totalEquals(3);
    }

    @Test
    public void testById() {

        e4().insertColumns("id")
                .values(2)
                .exec();

        Response response = target("/e4/2").request().get();

        onSuccess(response).bodyEquals(1, "{\"id\":2,\"cBoolean\":null," +
                "\"cDate\":null," +
                "\"cDecimal\":null," +
                "\"cInt\":null," +
                "\"cTime\":null," +
                "\"cTimestamp\":null," +
                "\"cVarchar\":null}");
    }

    @Test
    public void testById_Params() {

        e4().insertColumns("id")
                .values(2)
                .exec();

        Response response1 = target("/e4/2").request().get();
        onSuccess(response1).bodyEquals(1, "{\"id\":2,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,"
                + "\"cInt\":null,\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":null}");

        Response response2 = target("/e4/2").queryParam("include", "id").request().get();
        onSuccess(response2).bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testById_NotFound() {

        Response response = target("/e4/2").request().get();
        onResponse(response).statusEquals(Response.Status.NOT_FOUND)
                .bodyEquals("{\"success\":false,\"message\":\"No object for ID '2' and entity 'E4'\"}");
    }

    @Test
    public void testById_IncludeRelationship() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response response1 = target("/e3/8").queryParam("include", "e2.id").request().get();
        onSuccess(response1).bodyEquals(1, "{\"id\":8,\"e2\":{\"id\":1},\"name\":\"yyy\",\"phoneNumber\":null}");

        Response response2 = target("/e3/8").queryParam("include", "e2.name").request().get();
        onSuccess(response2).bodyEquals(1, "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\",\"phoneNumber\":null}");

        Response response3 = target("/e2/1").queryParam("include", "e3s.id").request().get();
        onSuccess(response3).bodyEquals(1, "{\"id\":1,\"address\":null,\"e3s\":[{\"id\":8},{\"id\":9}],\"name\":\"xxx\"}");
    }

    @Test
    public void testRelationshipSort() {

        e2().insertColumns("id_", "name")
                .values(1, "zzz")
                .values(2, "yyy")
                .values(3, "xxx").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "aaa", 1)
                .values(9, "bbb", 2)
                .values(10, "ccc", 3).exec();

        Response response = target("/e3")
                .queryParam("include", "id")
                .queryParam("include", E3.E2.getName())
                .queryParam("sort", E3.E2.dot(E2.NAME).getName())
                .request()
                .get();

        onSuccess(response).bodyEquals(3,
                "{\"id\":10,\"e2\":{\"id\":3,\"address\":null,\"name\":\"xxx\"}}",
                "{\"id\":9,\"e2\":{\"id\":2,\"address\":null,\"name\":\"yyy\"}}",
                "{\"id\":8,\"e2\":{\"id\":1,\"address\":null,\"name\":\"zzz\"}}");
    }

    @Test
    public void testRelationshipStartLimit() throws UnsupportedEncodingException {

        e2().insertColumns("id_", "name")
                .values(1, "zzz")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "aaa", 1)
                .values(9, "bbb", 1)
                .values(10, "ccc", 2).exec();

        Response response = target("/e2")
                .queryParam("include", "id")
                .queryParam("include", URLEncoder.encode("{\"path\":\"" + E2.E3S.getName() + "\",\"start\":1,\"limit\":1}", "UTF-8"))
                .queryParam("exclude", E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .request().get();

        onSuccess(response).bodyEquals(2,
                "{\"id\":1,\"e3s\":[{\"id\":9,\"name\":\"bbb\"}]}",
                "{\"id\":2,\"e3s\":[]}");
    }

    @Test
    public void testToOne_Null() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null).exec();

        Response response = target("/e3")
                .queryParam("include", "e2.id", "id")
                .request()
                .get();

        onSuccess(response).bodyEquals(2,
                "{\"id\":8,\"e2\":{\"id\":1}}",
                "{\"id\":9,\"e2\":null}");
    }

    @Test
    public void testCharPK() {

        e6().insertColumns("char_id", "char_column").values("a", "aaa").exec();

        Response response = target("/e6/a").request().get();
        onSuccess(response).bodyEquals(1, "{\"id\":\"a\",\"charColumn\":\"aaa\"}");
    }

    @Test
    public void testByCompoundId() {

        e17().insertColumns("id1", "id2", "name").values(1, 1, "aaa").exec();

        Response response = target("/e17")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .request()
                .get();

        onSuccess(response).bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"aaa\"}");
    }

    @Test
    public void testMapByRootEntity() {

        e4().insertColumns("c_varchar", "c_int").values("xxx", 1)
                .values("yyy", 2)
                .values("zzz", 2).exec();

        Response response = target("/e4")
                .queryParam("mapBy", "cInt")
                .queryParam("include", "cVarchar")
                .request()
                .get();

        onSuccess(response).bodyEqualsMapBy(3,
                "\"1\":[{\"cVarchar\":\"xxx\"}]",
                "\"2\":[{\"cVarchar\":\"yyy\"},{\"cVarchar\":\"zzz\"}]");
    }

    @Test
    public void testMapBy_RelatedId() {

        e2().insertColumns("id_", "name")
                .values(1, "zzz")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "aaa", 1)
                .values(9, "bbb", 1)
                .values(10, "ccc", 2).exec();

        Response response = target("/e3")
                .queryParam("mapBy", "e2.id")
                .queryParam("exclude", "phoneNumber")
                .request()
                .get();

        onSuccess(response).bodyEqualsMapBy(3,
                "\"1\":[{\"id\":8,\"name\":\"aaa\"},{\"id\":9,\"name\":\"bbb\"}]",
                "\"2\":[{\"id\":10,\"name\":\"ccc\"}]");
    }

    @Test
    public void testMapBy_OverRelationship() {

        e2().insertColumns("id_", "name")
                .values(1, "zzz")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "aaa", 1)
                .values(9, "bbb", 1)
                .values(10, "ccc", 2).exec();

        Response response = target("/e3")
                .queryParam("mapBy", "e2")
                .queryParam("exclude", "phoneNumber")
                .request()
                .get();

        onSuccess(response).bodyEqualsMapBy(3,
                "\"1\":[{\"id\":8,\"name\":\"aaa\"},{\"id\":9,\"name\":\"bbb\"}]",
                "\"2\":[{\"id\":10,\"name\":\"ccc\"}]");
    }

    @Test
    public void testById_EscapeLineSeparators() {

        e4().insertColumns("id", "c_varchar").values(1, "First line\u2028Second line...\u2029").exec();

        Response response = target("/e4/1")
                .queryParam("include", "cVarchar")
                .request()
                .get();

        onSuccess(response).bodyEquals(1, "{\"cVarchar\":\"First line\\u2028Second line...\\u2029\"}");
    }

    @Test
    public void testByteArrayProperty() throws IOException {

        e19().insertColumns("id", "guid").values(35, "someValue123".getBytes("UTF-8")).exec();

        Response response = target("/e19/35")
                .queryParam("include", E19.GUID.getName())
                .request()
                .get();

        onSuccess(response).bodyEquals(1, "{\"guid\":\"c29tZVZhbHVlMTIz\"}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E2.class).uri(uriInfo).get();
        }

        @GET
        @Path("e2/{id}")
        public DataResponse<E2> getE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.service(config).selectById(E2.class, id, uriInfo);
        }

        @GET
        @Path("e3")
        public DataResponse<E3> getE3(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E3.class).uri(uriInfo).get();
        }

        @GET
        @Path("e3/{id}")
        public DataResponse<E3> getE3ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.service(config).selectById(E3.class, id, uriInfo);
        }

        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E4.class).uri(uriInfo).get();
        }

        @GET
        @Path("e4/{id}")
        public DataResponse<E4> getE4_WithIncludeExclude(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.service(config).selectById(E4.class, id, uriInfo);
        }

        @GET
        @Path("e6/{id}")
        public DataResponse<E6> getOneE6(@PathParam("id") String id) {
            return Ag.service(config).selectById(E6.class, id);
        }

        @GET
        @Path("e19/{id}")
        public DataResponse<E19> getById(@Context UriInfo uriInfo, @PathParam("id") Integer id) {
            return Ag.select(E19.class, config).uri(uriInfo).byId(id).getOne();
        }

        @GET
        @Path("e17")
        public DataResponse<E17> getByCompoundId(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2) {

            Map<String, Object> ids = new HashMap<>();
            ids.put(E17.ID1_PK_COLUMN, id1);
            ids.put(E17.ID2_PK_COLUMN, id2);

            return Ag.select(E17.class, config).uri(uriInfo).byId(ids).getOne();
        }
    }

}
