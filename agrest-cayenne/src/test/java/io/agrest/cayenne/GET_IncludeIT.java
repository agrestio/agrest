package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E5;
import io.agrest.cayenne.unit.CayenneAgTester;
import io.agrest.cayenne.unit.DbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

public class GET_IncludeIT extends DbTest {

    @BQTestTool
    static final CayenneAgTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E5.class)
            .build();

    @Test
    public void testRelated() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .get()
                .wasSuccess()
                .bodyEquals(2, "{\"id\":8,\"e2\":{\"id\":1}}", "{\"id\":9,\"e2\":{\"id\":1}}");

        tester.assertQueryCount(2);
    }

    @Test
    public void testOrderOfInclude() {

        tester.e5().insertColumns("id", "name", "date").values(45, "T", "2013-01-03").exec();
        tester.e2().insertColumns("id_", "name").values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id", "e5_id").values(3, "zzz", 8, 45).exec();

        tester.target("/e3")
                .queryParam("include", "id", "e2", "e2.id")
                .get().wasSuccess().bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":8}}");

        tester.assertQueryCount(2);

        // change the order of includes
        tester.target("/e3")
                .queryParam("include", "id", "e2.id", "e2")
                .get().wasSuccess().bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":8}}");
        tester.assertQueryCount(2 + 2);
    }

    @Test
    public void testPhantom() {
        tester.e5().insertColumns("id", "name", "date").values(45, "T", "2013-01-03").exec();
        tester.e2().insertColumns("id_", "name").values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id", "e5_id").values(3, "zzz", 8, 45).exec();

        tester.target("/e2")
                .queryParam("include", "id", "e3s.e5.id")
                .get()
                .wasSuccess()
                .bodyEquals(1, "{\"id\":8,\"e3s\":[{\"e5\":{\"id\":45}}]}");

        // TODO: actually expect only 2 queries .. "e3" is a phantom entity
        tester.assertQueryCount(3);
    }

    @Test
    public void testStartLimit() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1)
                .values(10, "zzz", 1)
                .values(11, "zzz", 1).exec();

        tester.target("/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .queryParam("start", "1")
                .queryParam("limit", "2")

                .get().wasSuccess().bodyEquals(4,
                "{\"id\":9,\"e2\":{\"id\":1}}",
                "{\"id\":10,\"e2\":{\"id\":1}}");

        tester.assertQueryCount(3);

        // TODO: while the query counter is correct, the queries are suspect:
        //  1. There are 4 queries, while our counter catches only 3 (the last query in paginated result is not reported).
        //  2. e2 is fetched via a join.. If we have lots of E2s, this will be problematic

// SELECT t0.id FROM utest.e3 t0 ORDER BY t0.id
//  === returned 4 rows. - took 5 ms.
//
// SELECT DISTINCT t0.address, t0.name, t0.id, t1.id FROM utest.e2 t0 JOIN utest.e3 t1 ON (t0.id = t1.e2_id)
//  === returned 4 rows. - took 1 ms.
//
// SELECT t0.name, t0.phone_number, t0.e2_id, t0.e5_id, t0.id FROM utest.e3 t0 WHERE (t0.id = ?) OR (t0.id = ?) [bind: 1->id:8, 2->id:9]
//=== returned 2 rows. - took 11 ms.
//
// SELECT t0.name, t0.phone_number, t0.e2_id, t0.e5_id, t0.id FROM utest.e3 t0 WHERE (t0.id = ?) OR (t0.id = ?) [bind: 1->id:10, 2->id:11]
//=== returned 2 rows. - took 1 ms.

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
        @Path("e3")
        public DataResponse<E3> getE3(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E3.class).uri(uriInfo).get();
        }
    }
}
