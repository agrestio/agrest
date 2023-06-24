package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E15;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E5;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

public class GET_IncludeIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E2.class, E3.class, E5.class, E15.class)
            .build();

    @Test
    public void related() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"id\":8,\"e2\":{\"id\":1}}", "{\"id\":9,\"e2\":{\"id\":1}}");

        tester.assertQueryCount(2);
    }

    @Test
    public void orderOfInclude() {

        tester.e5().insertColumns("id", "name", "date").values(45, "T", "2013-01-03").exec();
        tester.e2().insertColumns("id_", "name").values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id", "e5_id").values(3, "zzz", 8, 45).exec();

        tester.target("/e3")
                .queryParam("include", "id", "e2", "e2.id")
                .get().wasOk().bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":8}}");

        tester.assertQueryCount(2);

        // change the order of includes
        tester.target("/e3")
                .queryParam("include", "id", "e2.id", "e2")
                .get().wasOk().bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":8}}");
        tester.assertQueryCount(2 + 2);
    }

    @Test
    public void phantom() {
        tester.e5().insertColumns("id", "name", "date").values(45, "T", "2013-01-03").exec();
        tester.e2().insertColumns("id_", "name").values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id", "e5_id").values(3, "zzz", 8, 45).exec();

        tester.target("/e2")
                .queryParam("include", "id", "e3s.e5.id")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":8,\"e3s\":[{\"e5\":{\"id\":45}}]}");

        // TODO: actually expect only 2 queries .. "e3" is a phantom entity
        tester.assertQueryCount(3);
    }

    @Test
    public void phantom_OverExplicitJoinTable() {
        tester.e1().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();


        tester.e15().insertColumns("long_id", "name")
                .values(14L, "aaa")
                .values(15L, "bbb")
                .values(16L, "ccc").exec();

        tester.e15_1().insertColumns("e15_id", "e1_id")
                .values(14, 1).exec();


        tester.target("/e15")
                .queryParam("include", "id", "e15e1.e1")
                .get()
                .wasOk()
                .bodyEquals(3,
                        "{\"id\":14,\"e15e1\":[{\"e1\":{\"id\":1,\"age\":null,\"description\":null,\"name\":\"xxx\"}}]}",
                        "{\"id\":15,\"e15e1\":[]}",
                        "{\"id\":16,\"e15e1\":[]}");

        tester.assertQueryCount(3);
    }

    @Test
    public void startLimit() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1)
                .values(10, "zzz", 1)
                .values(11, "zzz", 1).exec();

        // aligned with Cayenne "page" boundaries
        tester.target("/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .queryParam("start", "2")
                .queryParam("limit", "2")

                .get().wasOk().bodyEquals(4,
                        "{\"id\":10,\"e2\":{\"id\":1}}",
                        "{\"id\":11,\"e2\":{\"id\":1}}");

        // There are 3 queries, while our counter catches only 2 (the last query in paginated result is not reported).
        tester.assertQueryCount(2);

        tester.target("/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .queryParam("start", "1")
                .queryParam("limit", "2")

                .get().wasOk().bodyEquals(4,
                        "{\"id\":9,\"e2\":{\"id\":1}}",
                        "{\"id\":10,\"e2\":{\"id\":1}}");

        // not aligned with Cayenne "page" boundaries ... extra query
        tester.assertQueryCount(2 + 3);
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
        @Path("e3")
        public DataResponse<E3> getE3(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E3.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e15")
        public DataResponse<E15> getE15(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E15.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }
    }
}
