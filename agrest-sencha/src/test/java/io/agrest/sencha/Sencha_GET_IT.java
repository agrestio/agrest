package io.agrest.sencha;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.SelectStage;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E5;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.sencha.ops.unit.SenchaBodyAssertions;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class Sencha_GET_IT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E5.class)
            .build();

    @Test
    public void testIncludeRelationships_ById() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/e3/8").queryParam("include", "e2.id")
                .get()
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":8,\"e2\":{\"id\":1},\"e2_id\":1,\"name\":\"yyy\",\"phoneNumber\":null}");

        tester.target("/e3/8").queryParam("include", "e2.name").get()
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"e2_id\":1,\"name\":\"yyy\",\"phoneNumber\":null}");

        tester.target("/e2/1").queryParam("include", "e3s.id").get()
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":1,\"address\":null,\"e3s\":[{\"id\":8},{\"id\":9}],\"name\":\"xxx\"}");
    }

    @Test
    public void testIncludeRelationships() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(2,
                        "{\"id\":8,\"e2\":{\"id\":1},\"e2_id\":1}",
                        "{\"id\":9,\"e2\":{\"id\":1},\"e2_id\":1}");
    }

    @Test
    public void testIncludeRelationships_StartLimit() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1)
                .values(10, "zzz", 1)
                .values(11, "zzz", 1).exec();

        tester.target("/e3")
                .queryParam("include", "id")
                .queryParam("include", "e2.id")
                .queryParam("sort", "id")
                .queryParam("start", "1")
                .queryParam("limit", "2").get()
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(4,
                        "{\"id\":9,\"e2\":{\"id\":1},\"e2_id\":1}",
                        "{\"id\":10,\"e2\":{\"id\":1},\"e2_id\":1}");
    }

    @Test
    public void testToOne_Null() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null).exec();

        tester.target("/e3").queryParam("include", "e2.id", "id").get()
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(2,
                        "{\"id\":8,\"e2\":{\"id\":1},\"e2_id\":1}",
                        "{\"id\":9,\"e2\":null,\"e2_id\":null}");
    }

    @Test
    public void testMapBy_ToOne() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1).exec();

        tester.target("/e3").queryParam("include", "{\"path\":\"e2\",\"mapBy\":\"name\"}", "id")
                .get()
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":8,\"e2\":{\"id\":1,\"address\":null,\"name\":\"xxx\"},\"e2_id\":1}");
    }

    @Test
    public void testToMany_IncludeRelated() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();
        tester.e5().insertColumns("id", "name").values(345, "B").values(346, "A").exec();
        tester.e3().insertColumns("id_", "name", "e2_id", "e5_id")
                .values(8, "a", 1, 345)
                .values(9, "z", 1, 345)
                .values(7, "m", 1, 346).exec();

        tester.target("/e2")
                .queryParam("include", "id", "{\"path\":\"e3s\"}", "e3s.e5.name")
                .queryParam("exclude", "e3s.id", "e3s.phoneNumber")
                .get()
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":1,\"e3s\":["
                        + "{\"e5\":{\"name\":\"B\"},\"e5_id\":345,\"name\":\"a\"},"
                        + "{\"e5\":{\"name\":\"A\"},\"e5_id\":346,\"name\":\"m\"},"
                        + "{\"e5\":{\"name\":\"B\"},\"e5_id\":345,\"name\":\"z\"}"
                        + "]}");
    }

    @Test
    public void testIncludePathRelationship() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(8, "yyy", 1).exec();

        tester.target("/e3")
                .queryParam("include", "id", "{\"path\":\"e2\"}").get()
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":8,\"e2\":{\"id\":1,\"address\":null,\"name\":\"xxx\"},\"e2_id\":1}");
    }

    @Test
    public void testFilter_ById() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();

        tester.target("/e2")
                .queryParam("include", "id")
                .queryParam("filter", "[{\"exactMatch\":true,\"disabled\":false,\"property\":\"id\",\"operator\":\"=\",\"value\":1}]")
                .get()
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":1}");
    }

    @Test
    public void testStartsWith_AfterParseRequest() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "Axx")
                .values(2, "Bxx")
                .values(3, "cxx").exec();

        tester.target("/e2_startwith_pr")
                .queryParam("include", "id")
                .queryParam("query", "a")
                .queryParam("sort", "id").get()
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":1}");

        tester.target("/e2_startwith_pr")
                .queryParam("include", "id")
                .queryParam("query", "C")
                .queryParam("sort", "id").get()
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":3}");
    }

    @Test
    public void testStartsWith_AfterAssembleQuery() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "Axx")
                .values(2, "Bxx")
                .values(3, "cxx").exec();

        tester.target("/e2_startwith_aq")
                .queryParam("include", "id")
                .queryParam("query", "a")
                .queryParam("sort", "id").get()
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":1}");

        tester.target("/e2_startwith_aq")
                .queryParam("include", "id")
                .queryParam("query", "C")
                .queryParam("sort", "id").get()
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":3}");
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
        @Path("e2/{id}")
        public DataResponse<E2> getE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.select(E2.class, config).uri(uriInfo).byId(id).get();
        }

        @GET
        @Path("e3")
        public DataResponse<E3> get(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E3.class).uri(uriInfo).get();
        }

        @GET
        @Path("e3/{id}")
        public DataResponse<E3> getById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.select(E3.class, config).uri(uriInfo).byId(id).get();
        }

        @GET
        @Path("e2_startwith_pr")
        public DataResponse<E2> getE2_StartsWith_ParseRequest(@Context UriInfo uriInfo) {
            return Ag
                    .service(config)
                    .select(E2.class)
                    .stage(SelectStage.CREATE_ENTITY, SenchaOps.startsWithFilter(E2.NAME.getName(), uriInfo))
                    .uri(uriInfo).get();
        }

        @GET
        @Path("e2_startwith_aq")
        public DataResponse<E2> getE2_StartsWith_AssembleQuery(@Context UriInfo uriInfo) {
            return Ag
                    .service(config)
                    .select(E2.class)
                    .stage(SelectStage.APPLY_SERVER_PARAMS, SenchaOps.startsWithFilter(E2.NAME.getName(), uriInfo))
                    .uri(uriInfo).get();
        }
    }
}
