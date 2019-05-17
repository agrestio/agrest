package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E5;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class GET_IncludeIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E5.class};
    }

    @Test
    public void testRelated() {

        e2().insertColumns("id", "name")
                .values(1, "xxx").exec();

        e3().insertColumns("id", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r = target("/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .request()
                .get();

        onSuccess(r)
                .bodyEquals(2, "{\"id\":8,\"e2\":{\"id\":1}}", "{\"id\":9,\"e2\":{\"id\":1}}")
                .ranQueries(2);
    }

    @Test
    public void testOrderOfInclude() {

        e5().insertColumns("id", "name", "date").values(45, "T", "2013-01-03").exec();
        e2().insertColumns("id", "name").values(8, "yyy").exec();
        e3().insertColumns("id", "name", "e2_id", "e5_id").values(3, "zzz", 8, 45).exec();

        Response r1 = target("/e3")
                .queryParam("include", "id", "e2", "e2.id")
                .request()
                .get();

        onSuccess(r1).bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":8}}").ranQueries(2);

        // change the order of includes
        Response r2 = target("/e3")
                .queryParam("include", "id", "e2.id", "e2")
                .request()
                .get();

        onSuccess(r2).bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":8}}").ranQueries(2 + 2);
    }

    @Test
    public void testPhantom() {
        e5().insertColumns("id", "name", "date").values(45, "T", "2013-01-03").exec();
        e2().insertColumns("id", "name").values(8, "yyy").exec();
        e3().insertColumns("id", "name", "e2_id", "e5_id").values(3, "zzz", 8, 45).exec();

        Response r = target("/e2")
                .queryParam("include", "id", "e3s.e5.id")
                .request()
                .get();

        // TODO: actually expect only 2 queries .. "e3" is a phantom entity
        onSuccess(r).bodyEquals(1, "{\"id\":8,\"e3s\":[{\"e5\":{\"id\":45}}]}").ranQueries(3);
    }

    @Test
    public void testStartLimit() {

        e2().insertColumns("id", "name").values(1, "xxx").exec();

        e3().insertColumns("id", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1)
                .values(10, "zzz", 1)
                .values(11, "zzz", 1).exec();

        Response r = target("/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .queryParam("start", "1")
                .queryParam("limit", "2")
                .request()
                .get();

        onSuccess(r).bodyEquals(4,
                "{\"id\":9,\"e2\":{\"id\":1}}",
                "{\"id\":10,\"e2\":{\"id\":1}}")
                .ranQueries(3);

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
