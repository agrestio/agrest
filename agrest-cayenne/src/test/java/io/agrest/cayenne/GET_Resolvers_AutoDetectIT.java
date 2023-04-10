package io.agrest.cayenne;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
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

public class GET_Resolvers_AutoDetectIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class)
            .build();

    @Test
    public void test_ViaParentExpResolver() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .values(10, "aaa", 2)
                .exec();

        tester.target("/e3")
                .queryParam("include", "id", "name", "e2.name")
                .get()
                .wasOk()
                .bodyEquals(3,
                        "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\"}",
                        "{\"id\":9,\"e2\":null,\"name\":\"zzz\"}",
                        "{\"id\":10,\"e2\":{\"name\":\"yyy\"},\"name\":\"aaa\"}");

        tester.assertQueryCount(2);
    }

    @Test
    public void test_ViaParentIdsResolver() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .values(10, "aaa", 2)
                .values(11, "bbb", 2)
                .values(12, "ccc", 2)
                .exec();

        tester.target("/e3")
                .queryParam("include", "id", "name", "e2.name")
                .queryParam("start", 1)
                .queryParam("limit", 2)
                .get()
                .wasOk()
                .bodyEquals(5,
                        "{\"id\":9,\"e2\":null,\"name\":\"zzz\"}",
                        "{\"id\":10,\"e2\":{\"name\":\"yyy\"},\"name\":\"aaa\"}");

        tester.assertQueryCount(3);
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e3")
        public DataResponse<E3> e3(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E3.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }
    }
}
