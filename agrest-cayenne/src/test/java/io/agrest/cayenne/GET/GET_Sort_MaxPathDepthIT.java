package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.SelectBuilder;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
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

public class GET_Sort_MaxPathDepthIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E2.class, E3.class)
            .build();

    @BeforeEach
    void insertTestData() {
        tester.e2().insertColumns("id_", "name").values(8, "yyy").values(7, "aaa").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).values(4, "bbb", 7).exec();
    }

    @Test
    public void testDepth100_Default() {
        tester.target("/e3")
                .queryParam("include", "id")
                .queryParam("sort", "e2.name")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"id\":4}", "{\"id\":3}");
    }

    @Test
    public void testDepth0() {
        tester.target("/e3")
                .queryParam("depth", 0)
                .queryParam("include", "id")
                .queryParam("sort", "name")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"id\":4}", "{\"id\":3}");
    }

    @Test
    public void testDepth0_ByRelated() {
        tester.target("/e3")
                .queryParam("depth", 0)
                .queryParam("include", "id")
                .queryParam("sort", "e2.name")
                .get()
                .wasBadRequest();
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e3")
        public DataResponse<E3> getE3(
                @Context UriInfo uriInfo,

                // This is for test only. Don't do that at home. Max include depth must not be
                // controlled by the client
                @QueryParam("depth") Integer depth) {

            SelectBuilder<E3> builder = AgJaxrs
                    .select(E3.class, config)
                    .clientParams(uriInfo.getQueryParameters());

            if (depth != null) {
                builder.maxPathDepth(depth);
            }

            return builder.get();
        }
    }
}
