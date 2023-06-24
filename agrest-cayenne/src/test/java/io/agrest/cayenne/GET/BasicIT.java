package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E17;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E29;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E31;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.cayenne.main.E6;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class BasicIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class, E6.class, E17.class, E31.class)
            .entitiesAndDependencies(E29.class)
            .build();

    @Test
    public void basic() {

        tester.e4().insertColumns("id", "c_varchar", "c_int").values(1, "xxx", 5).exec();

        tester.target("/e4")
                .get()
                .wasOk()
                .bodyEquals(1,
                        "{\"id\":1,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,"
                                + "\"cInt\":5,\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"xxx\"}");
    }

    @Test
    public void idCalledId() {

        tester.e31().insertColumns("id", "name").values(5, "30").values(4, "31").exec();

        tester.target("/e31")
                .queryParam("sort", "name")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"id\":5,\"name\":\"30\"}", "{\"id\":4,\"name\":\"31\"}");
    }

    @Test
    public void byId() {

        tester.e4().insertColumns("id")
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
    public void byId_Params() {

        tester.e4().insertColumns("id")
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
    public void byId_NotFound() {
        tester.target("/e4/2").get()
                .wasNotFound()
                .bodyEquals("{\"message\":\"No object for ID '2' and entity 'E4'\"}");
    }

    @Test
    public void byId_IncludeRelationship() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
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

    @Test
    public void relationshipSort() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "zzz")
                .values(2, "yyy")
                .values(3, "xxx").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "aaa", 1)
                .values(9, "bbb", 2)
                .values(10, "ccc", 3).exec();

        tester.target("/e3")
                .queryParam("include", "id")
                .queryParam("include", E3.E2.getName())
                .queryParam("sort", E3.E2.dot(E2.NAME).getName())

                .get().wasOk().bodyEquals(3,
                "{\"id\":10,\"e2\":{\"id\":3,\"address\":null,\"name\":\"xxx\"}}",
                "{\"id\":9,\"e2\":{\"id\":2,\"address\":null,\"name\":\"yyy\"}}",
                "{\"id\":8,\"e2\":{\"id\":1,\"address\":null,\"name\":\"zzz\"}}");
    }

    @Test
    public void relationshipStartLimit() {

        // TODO: run this test with different combinations of Resolvers.
        //  Suspect that not all resolvers would support limits filtering of the result

        tester.e2().insertColumns("id_", "name")
                .values(1, "zzz")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "aaa", 1)
                .values(9, "bbb", 1)
                .values(10, "ccc", 2).exec();

        tester.target("/e2")
                .queryParam("include", "id")
                .queryParam("include", "{\"path\":\"" + E2.E3S.getName() + "\",\"start\":1,\"limit\":1}")
                .queryParam("exclude", E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":1,\"e3s\":[{\"id\":9,\"name\":\"bbb\"}]}",
                        "{\"id\":2,\"e3s\":[]}");
    }

    @Test
    public void toOne_Null() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null).exec();

        tester.target("/e3")
                .queryParam("include", "e2.id", "id")

                .get().wasOk().bodyEquals(2,
                "{\"id\":8,\"e2\":{\"id\":1}}",
                "{\"id\":9,\"e2\":null}");
    }

    @Test
    public void charPK() {

        tester.e6().insertColumns("char_id", "char_column").values("a", "aaa").exec();

        tester.target("/e6/a").get().wasOk().bodyEquals(1, "{\"id\":\"a\",\"charColumn\":\"aaa\"}");
    }

    @Test
    public void byCompoundId() {

        tester.e17().insertColumns("id1", "id2", "name").values(1, 1, "aaa").exec();

        tester.target("/e17")
                .queryParam("id1", 1)
                .queryParam("id2", 1)

                .get().wasOk().bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"aaa\"}");
    }

    @Test
    // Reproduces https://github.com/agrestio/agrest/issues/478
    public void compoundId_PartiallyMapped_DiffPropNames() {

        tester.e29().insertColumns("id1", "id2").values(1, 15).exec();
        tester.target("/e29")
                .get()
                .wasOk()
                // "id1" is a DB column name, "id2Prop" is an object property name
                .bodyEquals(1, "{\"id\":{\"db:id1\":1,\"id2Prop\":15},\"id2Prop\":15}");
    }

    @Test
    public void byCompoundDbId() {

        tester.e29().insertColumns("id1", "id2")
                .values(1, 15)
                .values(2, 35).exec();

        tester.target("/e29_compound_db")
                .queryParam("id1", 1)
                .queryParam("id2", 15)
                .get().wasOk()
                .bodyEquals(1, "{\"id\":{\"db:id1\":1,\"id2Prop\":15},\"id2Prop\":15}");
    }

    @Test
    public void byId_EscapeLineSeparators() {

        tester.e4().insertColumns("id", "c_varchar").values(1, "First line\u2028Second line...\u2029").exec();

        tester.target("/e4/1")
                .queryParam("include", "cVarchar")

                .get().wasOk().bodyEquals(1, "{\"cVarchar\":\"First line\\u2028Second line...\\u2029\"}");
    }


    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

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
        @Path("e17")
        public DataResponse<E17> getByCompoundId(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2) {

            Map<String, Object> ids = new HashMap<>();
            ids.put(E17.ID1.getName(), id1);
            ids.put(E17.ID2.getName(), id2);

            return AgJaxrs.select(E17.class, config).clientParams(uriInfo.getQueryParameters()).byId(ids).getOne();
        }

        @GET
        @Path("e29")
        public DataResponse<E29> getAllE29s(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E29.class, config).clientParams(uriInfo.getQueryParameters()).getOne();
        }

        @GET
        @Path("e29_compound_db")
        public DataResponse<E29> getByCompoundDbId(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2) {

            Map<String, Object> ids = new HashMap<>();
            ids.put("db:" + E29.ID1_PK_COLUMN, id1);
            ids.put(E29.ID2PROP.getName(), id2);

            return AgJaxrs.select(E29.class, config).clientParams(uriInfo.getQueryParameters()).byId(ids).getOne();
        }

        @GET
        @Path("e31")
        public DataResponse<E31> getAllE31s(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E31.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }
    }

}
