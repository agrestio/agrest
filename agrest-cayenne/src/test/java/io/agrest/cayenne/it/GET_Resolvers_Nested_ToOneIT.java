package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.CayenneResolvers;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class GET_Resolvers_Nested_ToOneIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class};
    }

    @Test
    public void test_JointPrefetchResolver() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        Response r = target("/e3_joint_prefetch")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e2.name")
                .queryParam("cayenneExp", "id > 3")
                .request().get();


        onSuccess(r)
                .bodyEquals(2, "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\"},{\"id\":9,\"e2\":null,\"name\":\"zzz\"}")
                .ranQueries(1);
    }

    @Test
    public void test_QueryWithParentIdsResolver() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        Response r = target("/e3_query_with_parent_ids")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e2.name")
                .queryParam("cayenneExp", "id > 3")
                .request().get();

        onSuccess(r)
                .bodyEquals(2, "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\"},{\"id\":9,\"e2\":null,\"name\":\"zzz\"}")
                .ranQueries(2);
    }

    @Test
    public void test_QueryWithParentIdsResolver_Pagination() {

        e2().insertColumns("id_", "name")
                .values(1, "aaa")
                .values(2, "bbb")
                .values(3, "ccc")
                .values(4, "ddd")
                .exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "aaa3", 1)
                .values(9, "bbb3", 2)
                .values(10, "ccc3", 3)
                .values(11, "ddd3", 4)
                .exec();

        Response r = target("/e3_query_with_parent_ids")
                .queryParam("include", "id")
                .queryParam("include", "e2.id")
                .queryParam("sort", "id")
                .queryParam("limit", 2)
                .request().get();

        onSuccess(r)
                .bodyEquals(4,
                        "{\"id\":8,\"e2\":{\"id\":1}}",
                        "{\"id\":9,\"e2\":{\"id\":2}}")
                .ranQueries(2);
    }

    @Test
    public void test_QueryWithParentQualifierResolver() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        Response r = target("/e3_query_with_parent_qualifier")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e2.name")
                .queryParam("cayenneExp", "id > 3")
                .request().get();

        onSuccess(r)
                .bodyEquals(2, "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\"},{\"id\":9,\"e2\":null,\"name\":\"zzz\"}")
                .ranQueries(2);
    }

    @Test
    public void test_QueryWithParentQualifierResolver_NoFetchIfNoParent() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        Response r = target("/e3_query_with_parent_qualifier")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e2.name")
                .queryParam("cayenneExp", "id > 9")
                .request().get();


        onSuccess(r)
                .bodyEquals(0, "")
                .ranQueries(1);
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e3_joint_prefetch")
        public DataResponse<E3> e3_joint_prefetch(@Context UriInfo uriInfo) {

            // non-standard nested resolver
            AgEntityOverlay<E3> e3Overlay = AgEntity
                    .overlay(E3.class)
                    .redefineRelationshipResolver("e2", CayenneResolvers.nested(config).viaParentPrefetch());

            return Ag.select(E3.class, config)
                    .entityOverlay(e3Overlay)
                    .uri(uriInfo)
                    .get();
        }

        @GET
        @Path("e3_query_with_parent_ids")
        public DataResponse<E3> e3_query_with_parent_ids(@Context UriInfo uriInfo) {

            // non-standard nested resolver
            AgEntityOverlay<E3> e3Overlay = AgEntity
                    .overlay(E3.class)
                    .redefineRelationshipResolver("e2", CayenneResolvers.nested(config).viaQueryWithParentIds());

            return Ag.select(E3.class, config)
                    .entityOverlay(e3Overlay)
                    .uri(uriInfo)
                    .get();
        }

        @GET
        @Path("e3_query_with_parent_qualifier")
        public DataResponse<E3> e3_query_with_parent_qualifier(@Context UriInfo uriInfo) {

            // non-standard nested resolver
            AgEntityOverlay<E3> e3Overlay = AgEntity
                    .overlay(E3.class)
                    // this is actually the standard strategy, but let's see how it works if installed via a request-scoped overlay
                    .redefineRelationshipResolver("e2", CayenneResolvers.nested(config).viaQueryWithParentExp());

            return Ag.select(E3.class, config)
                    .entityOverlay(e3Overlay)
                    .uri(uriInfo)
                    .get();
        }
    }
}
