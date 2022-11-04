package io.agrest.cayenne;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class GET_SizeConstraintsIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E4.class)
            .build();

    // TODO: unclear what server-side fetch offset protects? so not testing it here.

    @Test
    public void testNoClientLimit() {

        tester.e4().insertColumns("id")
                .values(1)
                .values(2)
                .values(3).exec();

        tester.target("/e4/limit")
                .queryParam("sort", "id")
                .queryParam("include", "id")

                .get().wasOk().bodyEquals(3, "{\"id\":1},{\"id\":2}");
    }

    @Test
    public void testClientLimitBelowServerLimit() {

        tester.e4().insertColumns("id")
                .values(1)
                .values(2)
                .values(3).exec();

        tester.target("/e4/limit")
                .queryParam("sort", "id")
                .queryParam("include", "id")
                .queryParam("limit", "1")

                .get().wasOk().bodyEquals(3, "{\"id\":1}");
    }

    @Test
    public void testClientLimitExceedsServerLimit() {

        tester.e4().insertColumns("id")
                .values(1)
                .values(2)
                .values(3).exec();

        tester.target("/e4/limit")
                .queryParam("sort", "id")
                .queryParam("include", "id")
                .queryParam("limit", "5")

                .get().wasOk().bodyEquals(3, "{\"id\":1},{\"id\":2}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e4/limit")
        public DataResponse<E4> limit(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E4.class, config).clientParams(uriInfo.getQueryParameters())
                    .limit(2)
                    .get();
        }
    }
}
