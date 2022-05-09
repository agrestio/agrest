package io.agrest.jpa;

import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.agrest.DataResponse;
import io.agrest.converter.jsonvalue.UtcDateConverter;
import io.agrest.encoder.DateTimeFormatters;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.*;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

class GET_IT extends DbTest {

    @BQTestTool
    static final AgJpaTester tester = tester(Resource.class)
            .entities(E1.class, E3.class, E2.class, E4.class, E6.class, E17.class, E30.class,E28.class,E29.class)
            .build();

    @Test
    public void testResponse() {
        tester.e4().insertColumns("ID", "C_VARCHAR", "C_INT").values(1, "xxx", 5).exec();

        tester.target("/e4")
                .get()
                .wasOk()
                .bodyEquals(1,
                        "{\"id\":1,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,"
                                + "\"cInt\":5,\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"xxx\"}");
    }


    @Test
    public void testDateTime() {

        Date date = Date.from(Instant.from(UtcDateConverter.dateParser().fromString("2012-02-03T11:01:02Z")));
        tester.e4().insertColumns("C_TIMESTAMP").values(date).exec();

        String dateString = DateTimeFormatters.isoLocalDateTime().format(Instant.ofEpochMilli(date.getTime()));

        tester.target("/e4").queryParam("include", E4.C_TIMESTAMP).get()
                .wasOk().bodyEquals(1, "{\"cTimestamp\":\"" + dateString + "\"}");
    }

    @Test
    public void testDate() {

        Date date = Date.from(Instant.from(UtcDateConverter.dateParser().fromString("2012-02-03")));
        tester.e4().insertColumns("C_DATE").values(date).exec();

        String dateString = DateTimeFormatters.isoLocalDateTime().format(Instant.ofEpochMilli(date.getTime()));

        tester.target("/e4").queryParam("include", E4.C_DATE)
                .get()
                .wasOk()
                .bodyEquals(1, "{\"cDate\":\"" + dateString + "\"}");
    }

    @Test
    public void testTime() {

        LocalTime lt = LocalTime.of(14, 0, 1);

        // "14:00:01"
        Time time = Time.valueOf(lt);

        tester.e4().insertColumns("C_TIME").values(time).exec();

        String timeString = DateTimeFormatters.isoLocalDateTime().format(Instant.ofEpochMilli(time.getTime()));

        tester.target("/e4").queryParam("include", E4.C_TIME).get().wasOk().bodyEquals(1, "{\"cTime\":\"" + timeString + "\"}");
    }





    @Test
    public void testSort_ById() {
        tester.e4().insertColumns("ID")
                .values(2)
                .values(1)
                .values(3).exec();

        tester.target("/e4")
                .queryParam("sort", "[{\"property\":\"id\",\"direction\":\"DESC\"}]")
                .queryParam("include", "id")
                .get()
                .wasOk()
                .bodyEquals(3, "{\"id\":3}", "{\"id\":2}", "{\"id\":1}");
    }

    @Test
    public void testSort_Invalid() {
        tester.target("/e4")
                .queryParam("sort", "[{\"property\":\"xyz\",\"direction\":\"DESC\"}]")
                .queryParam("include", "id")
                .get()
                .wasBadRequest()
                .bodyEquals("{\"success\":false,\"message\":\"Invalid path 'xyz' for 'E4'\"}");
    }

    @Test
    // this is a hack for Sencha bug, passing us null sorters per LF-189...
    // allowing for lax property name checking as a result
    public void testSort_Null() {
        tester.e4().insertColumns("ID")
                .values(2)
                .values(1)
                .values(3).exec();

        tester.target("/e4")
                .queryParam("sort", "[{\"property\":null,\"direction\":\"DESC\"}]")
                .queryParam("include", "id")
                .get().wasOk()
                .totalEquals(3);
    }

    @Test
    public void testById() {
        tester.e4().insertColumns("ID")
                .values(2)
                .exec();

        tester.target("/e4/2").get().wasOk().bodyEquals(1, "{\"id\":2,\"cBoolean\":null," +
                "\"cDate\":null," +
                "\"cDecimal\":null," +
                "\"cInt\":null," +
                "\"cTime\":null," +
                "\"cTimestamp\":null," +
                "\"cVarchar\":null}");
    }

    @Test
    public void testById_Params() {
        tester.e4().insertColumns("ID")
                .values(2)
                .exec();

        tester.target("/e4/2").get()
                .wasOk()
                .bodyEquals(1, "{\"id\":2,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,"
                        + "\"cInt\":null,\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":null}");

        tester.target("/e4/2").queryParam("include", "id").get()
                .wasOk()
                .bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testById_NotFound() {
        tester.target("/e4/2").get()
                .wasNotFound()
                .bodyEquals("{\"success\":false,\"message\":\"No object for ID '2' and entity 'E4'\"}");
    }

    @Test
    public void testById_IncludeRelationship() {
        tester.e2().insertColumns("ID", "NAME").values(1, "xxx").exec();
        tester.e3().insertColumns("ID", "NAME", "E2_ID")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/e3/8").queryParam("include", "e2.id").get()
                .wasOk()
                .bodyEquals(1, "{\"id\":8,\"e2\":{\"id\":1},\"name\":\"yyy\",\"phoneNumber\":null}");

        tester.target("/e3/8").queryParam("include", "e2.name").get()
                .wasOk()
                .bodyEquals(1, "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\",\"phoneNumber\":null}");

        tester.target("/e2/1").queryParam("include", "e3s.id").get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"address\":null,\"e3s\":[{\"id\":8},{\"id\":9}],\"name\":\"xxx\"}");
    }

    //TODO testRelationshipSort
    //TODO testRelationshipStartLimit

    @Test
    public void testToOne_Null() {
        tester.e2().insertColumns("ID", "NAME").values(1, "xxx").exec();

        tester.e3().insertColumns("ID", "NAME", "E2_ID")
                .values(8, "yyy", 1)
                .values(9, "zzz", null).exec();

        tester.target("/e3")
                .queryParam("include", "e2.id", "id")

                .get().wasOk().bodyEquals(2,
                        "{\"id\":8,\"e2\":{\"id\":1}}",
                        "{\"id\":9,\"e2\":null}");
    }

    @Test
    public void testCharPK() {
        tester.e6().insertColumns("CHAR_ID", "CHAR_COLUMN").values("a", "aaa").exec();

        tester.target("/e6/a").get().wasOk().bodyEquals(1, "{\"id\":\"a\",\"charColumn\":\"aaa\"}");
    }

    @Test
    // Reproduces https://github.com/agrestio/agrest/issues/478
    public void testCompoundId_PartiallyMapped_DiffPropNames() {
        tester.e29().insertColumns("ID1", "ID2").values(1, 15).exec();
        tester.target("/e29")
                .get()
                .wasOk()
                // "id1" is a DB column name, "id2Prop" is an object property name
                .bodyEquals(1, "{\"id\":{\"db:id1\":1,\"id2Prop\":15},\"id2Prop\":15}");
    }


    @Test
    public void testMapByRootEntity() {

        tester.e4().insertColumns("C_VARCHAR", "C_INT").values("xxx", 1)
                .values("yyy", 2)
                .values("zzz", 2).exec();

        tester.target("/e4")
                .queryParam("mapBy", "cInt")
                .queryParam("include", "cVarchar")

                .get().wasOk().bodyEqualsMapBy(3,
                        "\"1\":[{\"cVarchar\":\"xxx\"}]",
                        "\"2\":[{\"cVarchar\":\"yyy\"},{\"cVarchar\":\"zzz\"}]");
    }

    @Test
    public void testMapBy_RelatedId() {

        tester.e2().insertColumns("ID", "NAME")
                .values(1, "zzz")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("ID", "NAME", "E2_ID")
                .values(8, "aaa", 1)
                .values(9, "bbb", 1)
                .values(10, "ccc", 2).exec();

        tester.target("/e3")
                .queryParam("mapBy", "e2.id")
                .queryParam("exclude", "phoneNumber")

                .get().wasOk().bodyEqualsMapBy(3,
                        "\"1\":[{\"id\":8,\"name\":\"aaa\"},{\"id\":9,\"name\":\"bbb\"}]",
                        "\"2\":[{\"id\":10,\"name\":\"ccc\"}]");
    }

    @Test
    public void testMapBy_OverRelationship() {

        tester.e2().insertColumns("ID", "NAME")
                .values(1, "zzz")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("ID", "NAME", "E2_ID")
                .values(8, "aaa", 1)
                .values(9, "bbb", 1)
                .values(10, "ccc", 2).exec();

        tester.target("/e3")
                .queryParam("mapBy", "e2")
                .queryParam("exclude", "phoneNumber")

                .get().wasOk().bodyEqualsMapBy(3,
                        "\"1\":[{\"id\":8,\"name\":\"aaa\"},{\"id\":9,\"name\":\"bbb\"}]",
                        "\"2\":[{\"id\":10,\"name\":\"ccc\"}]");
    }

    @Test
    public void testById_EscapeLineSeparators() {

        tester.e4().insertColumns("ID", "C_VARCHAR").values(1, "First line\u2028Second line...\u2029").exec();

        tester.target("/e4/1")
                .queryParam("include", "cVarchar")

                .get().wasOk().bodyEquals(1, "{\"cVarchar\":\"First line\\u2028Second line...\\u2029\"}");
    }


    @Test
    public void testByteArrayProperty() {

        tester.e19().insertColumns("ID", "GUID").values(35, "someValue123".getBytes(StandardCharsets.UTF_8)).exec();

        tester.target("/e19/35")
                .queryParam("include", E19.GUID)
                .get().wasOk().bodyEquals(1, "{\"guid\":\"c29tZVZhbHVlMTIz\"}");
    }



    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private javax.ws.rs.core.Configuration config;

        @GET
        @Path("e1")
        public DataResponse<E1> getE1(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E1.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e2")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E2.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e2/{id}")
        public DataResponse<E2> getE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return AgJaxrs.select(E2.class, config).clientParams(uriInfo.getQueryParameters()).byId(id).get();
        }

        @GET
        @Path("e3")
        public DataResponse<E3> getE3(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E3.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e3/{id}")
        public DataResponse<E3> getE3ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return AgJaxrs.select(E3.class, config).clientParams(uriInfo.getQueryParameters()).byId(id).get();
        }

        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E4.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e4/{id}")
        public DataResponse<E4> getE4_WithIncludeExclude(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return AgJaxrs.select(E4.class, config).clientParams(uriInfo.getQueryParameters()).byId(id).get();
        }

        @GET
        @Path("e6/{id}")
        public DataResponse<E6> getOneE6(@PathParam("id") String id) {
            return AgJaxrs.select(E6.class, config).byId(id).get();
        }

        @GET
        @Path("e19/{id}")
        public DataResponse<E19> getById(@Context UriInfo uriInfo, @PathParam("id") Integer id) {
            return AgJaxrs.select(E19.class, config).clientParams(uriInfo.getQueryParameters()).byId(id).getOne();
        }

        @GET
        @Path("e29")
        public DataResponse<E29> getAllE29s(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E29.class, config).clientParams(uriInfo.getQueryParameters()).getOne();
        }


    }


}