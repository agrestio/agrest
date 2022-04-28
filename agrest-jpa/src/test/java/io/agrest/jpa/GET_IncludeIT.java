package io.agrest.jpa;

import io.agrest.DataResponse;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.E2;
import io.agrest.jpa.model.E3;
import io.agrest.jpa.model.E5;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

public class GET_IncludeIT extends DbTest {

    @BQTestTool
    static final AgJpaTester tester = tester(Resource.class)
            .entities( E3.class,E2.class, E5.class)
            .build();

    @Test
    public void testRelated() {

        tester.e2().insertColumns("ID", "NAME")
                .values(1, "xxx").exec();

        tester.e3().insertColumns("ID", "NAME", "E2_ID")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"id\":8,\"e2\":{\"id\":1}}", "{\"id\":9,\"e2\":{\"id\":1}}");

     //TODO   tester.assertQueryCount(2);
    }

    @Test
    public void testOrderOfInclude() {

        tester.e5().insertColumns("ID", "NAME", "DATE").values(45, "T", "2013-01-03").exec();
        tester.e2().insertColumns("ID", "NAME").values(8, "yyy").exec();
        tester.e3().insertColumns("ID", "NAME", "E2_ID", "E5_ID").values(3, "zzz", 8, 45).exec();

        tester.target("/e3")
                .queryParam("include", "id", "e2", "e2.id")
                .get().wasOk().bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":8}}");

     //   tester.assertQueryCount(2);

        // change the order of includes
        tester.target("/e3")
                .queryParam("include", "id", "e2.id", "e2")
                .get().wasOk().bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":8}}");
      //TODO  tester.assertQueryCount(2 + 2);
    }

    @Test
    public void testPhantom() {
        tester.e5().insertColumns("ID", "NAME", "DATE").values(45, "T", "2013-01-03").exec();
        tester.e2().insertColumns("ID", "NAME").values(8, "yyy").exec();
        tester.e3().insertColumns("ID", "NAME", "E2_ID", "E5_ID").values(3, "zzz", 8, 45).exec();

        tester.target("/e2")
                .queryParam("include", "id", "e3s.e5.id")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":8,\"e3s\":[{\"e5\":{\"id\":45}}]}");

        // TODO: actually expect only 2 queries .. "e3" is a phantom entity
     //TODO   tester.assertQueryCount(3);
    }

    @Test
    public void testStartLimit() {

        tester.e2().insertColumns("ID", "NAME").values(1, "xxx").exec();

        tester.e3().insertColumns("ID", "NAME", "E2_ID")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1)
                .values(10, "zzz", 1)
                .values(11, "zzz", 1).exec();

        tester.target("/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .queryParam("start", "1")
                .queryParam("limit", "2")

                .get().wasOk().bodyEquals(4,
                "{\"id\":9,\"e2\":{\"id\":1}}",
                "{\"id\":10,\"e2\":{\"id\":1}}");

        // There are 3 queries, while our counter catches only 2 (the last query in paginated result is not reported).
     //TODO   tester.assertQueryCount(2);

        // TODO: e2 is fetched via a join.. If we have lots of E2s, this will be problematic
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
    }
}
