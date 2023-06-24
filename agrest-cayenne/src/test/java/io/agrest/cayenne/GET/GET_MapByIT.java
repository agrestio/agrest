package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
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

public class GET_MapByIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E2.class, E3.class, E4.class)
            .build();

    @Test
    public void testMapByRootEntity() {

        tester.e4().insertColumns("c_varchar", "c_int").values("xxx", 1)
                .values("yyy", 2)
                .values("zzz", 2).exec();

        tester.target("/e4")
                .queryParam("mapBy", "cInt")
                .queryParam("include", "cVarchar")

                .get().wasOk().bodyEqualsMapBy(3,
                "\"1\":[{\"cVarchar\":\"xxx\"}]",
                "\"2\":[{\"cVarchar\":\"yyy\"},{\"cVarchar\":\"zzz\"}]");
    }

    @Test
    public void testMapBy_RelatedId() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "zzz")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "aaa", 1)
                .values(9, "bbb", 1)
                .values(10, "ccc", 2).exec();

        tester.target("/e3")
                .queryParam("mapBy", "e2.id")
                .queryParam("exclude", "phoneNumber")

                .get().wasOk().bodyEqualsMapBy(3,
                "\"1\":[{\"id\":8,\"name\":\"aaa\"},{\"id\":9,\"name\":\"bbb\"}]",
                "\"2\":[{\"id\":10,\"name\":\"ccc\"}]");
    }

    @Test
    public void testMapBy_OverRelationship() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "zzz")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "aaa", 1)
                .values(9, "bbb", 1)
                .values(10, "ccc", 2).exec();

        tester.target("/e3")
                .queryParam("mapBy", "e2")
                .queryParam("exclude", "phoneNumber")

                .get().wasOk().bodyEqualsMapBy(3,
                "\"1\":[{\"id\":8,\"name\":\"aaa\"},{\"id\":9,\"name\":\"bbb\"}]",
                "\"2\":[{\"id\":10,\"name\":\"ccc\"}]");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e3")
        public DataResponse<E3> getE3(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E3.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E4.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }
    }

}
