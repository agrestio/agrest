package io.agrest.cayenne;

import io.agrest.DataResponse;
import io.agrest.SelectBuilder;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E5;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

public class GET_Include_MaxPathDepthIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E2.class, E3.class, E5.class)
            .build();

    @BeforeEach
    void insertTestData() {
        tester.e5().insertColumns("id", "name", "date").values(45, "T", "2013-01-03").exec();
        tester.e2().insertColumns("id_", "name").values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id", "e5_id").values(3, "zzz", 8, 45).exec();
    }

    @Test
    public void testDepth100_Default() {

        tester.target("/e2")
                .queryParam("include", "id", "e3s.id", "e3s.e5.id")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":8,\"e3s\":[{\"id\":3,\"e5\":{\"id\":45}}]}");
    }

    @Test
    public void testDepth0_DefaultIncludes() {

        tester.target("/e2")
                .queryParam("depth", 0)
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":8,\"address\":null,\"name\":\"yyy\"}");
    }

    @Test
    public void testDepth0() {

        tester.target("/e2")
                .queryParam("depth", 0)
                .queryParam("include", "id", "e3s.id", "e3s.e5.id")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":8}");
    }

    @Test
    public void testDepth1() {

        tester.target("/e2")
                .queryParam("depth", 1)
                .queryParam("include", "id", "e3s.id", "e3s.e5.id")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":8,\"e3s\":[{\"id\":3}]}");
    }

    @Test
    public void testDepth2() {

        tester.target("/e2")
                .queryParam("depth", 2)
                .queryParam("include", "id", "e3s.id", "e3s.e5.id")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":8,\"e3s\":[{\"id\":3,\"e5\":{\"id\":45}}]}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2")
        public DataResponse<E2> getE2(
                @Context UriInfo uriInfo,

                // This is for test only. Don't do that at home. Max include depth must not be
                // controlled by the client
                @QueryParam("depth") Integer depth) {

            SelectBuilder<E2> builder = AgJaxrs
                    .select(E2.class, config)
                    .clientParams(uriInfo.getQueryParameters());

            if (depth != null) {
                builder.maxPathDepth(depth);
            }

            return builder.get();
        }
    }
}
