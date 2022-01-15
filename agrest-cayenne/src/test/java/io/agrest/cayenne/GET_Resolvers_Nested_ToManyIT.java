package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

public class GET_Resolvers_Nested_ToManyIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class)
            .build();

    @Test
    public void test_JointPrefetchResolver() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "aaa").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        tester.target("/e2_joint_prefetch")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e3s.name")
                .queryParam("exp", "id < 3")
                .queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":1,\"e3s\":[{\"name\":\"yyy\"}],\"name\":\"xxx\"}",
                        "{\"id\":2,\"e3s\":[],\"name\":\"aaa\"}");

        tester.assertQueryCount(1);
    }

    @Test
    public void test_QueryWithParentIdsResolver() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "aaa").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        tester.target("/e2_query_with_parent_ids")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e3s.name")
                .queryParam("exp", "id < 3")
                .queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":1,\"e3s\":[{\"name\":\"yyy\"}],\"name\":\"xxx\"}",
                        "{\"id\":2,\"e3s\":[],\"name\":\"aaa\"}");

        tester.assertQueryCount(2);
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2_joint_prefetch")
        public DataResponse<E2> e2_joint_prefetch(@Context UriInfo uriInfo) {

            AgEntityOverlay<E2> e2Overlay = AgEntity
                    .overlay(E2.class)
                    .redefineRelationshipResolver("e3s", CayenneResolvers.nestedViaParentPrefetch());

            return Ag.select(E2.class, config)
                    .entityOverlay(e2Overlay)
                    .uri(uriInfo)
                    .get();
        }

        @GET
        @Path("e2_query_with_parent_ids")
        public DataResponse<E2> e2_query_with_parent_ids(@Context UriInfo uriInfo) {

            AgEntityOverlay<E2> e2Overlay = AgEntity
                    .overlay(E2.class)
                    .redefineRelationshipResolver("e3s", CayenneResolvers.nestedViaQueryWithParentIds());

            return Ag.select(E2.class, config)
                    .entityOverlay(e2Overlay)
                    .uri(uriInfo)
                    .get();
        }
    }
}
