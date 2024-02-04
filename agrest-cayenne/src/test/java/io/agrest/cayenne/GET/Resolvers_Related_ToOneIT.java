package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.CayenneResolvers;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

public class Resolvers_Related_ToOneIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class)
            .build();

    @Test
    public void jointPrefetchResolver() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        tester.target("/e3_joint_prefetch")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e2.name")
                .queryParam("exp", "id > 3")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\"}",
                        "{\"id\":9,\"e2\":null,\"name\":\"zzz\"}");

        tester.assertQueryCount(1);
    }

    @Test
    public void queryWithParentIdsResolver() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        tester.target("/e3_query_with_parent_ids")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e2.name")
                .queryParam("exp", "id > 3")
                .get().wasOk()
                .bodyEquals(2,
                        "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\"}",
                        "{\"id\":9,\"e2\":null,\"name\":\"zzz\"}");

        tester.assertQueryCount(2);
    }

    @Test
    public void queryWithParentIdsResolver_Pagination() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "aaa")
                .values(2, "bbb")
                .values(3, "ccc")
                .values(4, "ddd")
                .exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "aaa3", 1)
                .values(9, "bbb3", 2)
                .values(10, "ccc3", 3)
                .values(11, "ddd3", 4)
                .exec();

        tester.target("/e3_query_with_parent_ids")
                .queryParam("include", "id")
                .queryParam("include", "e2.id")
                .queryParam("sort", "id")
                .queryParam("limit", 2)
                .get().wasOk()
                .bodyEquals(4,
                        "{\"id\":8,\"e2\":{\"id\":1}}",
                        "{\"id\":9,\"e2\":{\"id\":2}}");

        tester.assertQueryCount(2);
    }

    @Test
    public void queryWithParentQualifierResolver() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        tester.target("/e3_query_with_parent_qualifier")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e2.name")
                .queryParam("exp", "id > 3")
                .get().wasOk()
                .bodyEquals(2,
                        "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\"}",
                        "{\"id\":9,\"e2\":null,\"name\":\"zzz\"}");

        tester.assertQueryCount(2);
    }

    @Test
    public void queryWithParentQualifierResolver_NoFetchIfNoParent() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        tester.target("/e3_query_with_parent_qualifier")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e2.name")
                .queryParam("exp", "id > 9")
                .get().wasOk()
                .bodyEquals(0);

        tester.assertQueryCount(1);
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e3_joint_prefetch")
        public DataResponse<E3> e3_joint_prefetch(@Context UriInfo uriInfo) {

            // non-standard related resolver
            AgEntityOverlay<E3> e3Overlay = AgEntity
                    .overlay(E3.class)
                    .relatedDataResolver("e2", CayenneResolvers.relatedViaParentPrefetch());

            return AgJaxrs.select(E3.class, config)
                    .entityOverlay(e3Overlay)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }

        @GET
        @Path("e3_query_with_parent_ids")
        public DataResponse<E3> e3_query_with_parent_ids(@Context UriInfo uriInfo) {

            // non-standard related resolver
            AgEntityOverlay<E3> e3Overlay = AgEntity
                    .overlay(E3.class)
                    .relatedDataResolver("e2", CayenneResolvers.relatedViaQueryWithParentIds());

            return AgJaxrs.select(E3.class, config)
                    .entityOverlay(e3Overlay)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }

        @GET
        @Path("e3_query_with_parent_qualifier")
        public DataResponse<E3> e3_query_with_parent_qualifier(@Context UriInfo uriInfo) {

            // non-standard related resolver
            AgEntityOverlay<E3> e3Overlay = AgEntity
                    .overlay(E3.class)
                    // this is actually the standard strategy, but let's see how it works if installed via a request-scoped overlay
                    .relatedDataResolver("e2", CayenneResolvers.relatedViaQueryWithParentExp());

            return AgJaxrs.select(E3.class, config)
                    .entityOverlay(e3Overlay)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }
    }
}
