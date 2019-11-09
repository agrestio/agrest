package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.runtime.cayenne.CayenneResolvers;
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

public class GET_Resolvers_Nested_ToManyIT extends JerseyAndDerbyCase {

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

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "aaa").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        Response r = target("/e2_joint_prefetch")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e3s.name")
                .queryParam("cayenneExp", "id < 3")
                .queryParam("sort", "id")
                .request().get();

        onSuccess(r)
                .bodyEquals(2, "{\"id\":1,\"e3s\":[{\"name\":\"yyy\"}],\"name\":\"xxx\"},{\"id\":2,\"e3s\":[],\"name\":\"aaa\"}")
                .ranQueries(1);
    }

    @Test
    public void test_DisjointPrefetchResolver() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "aaa").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        Response r = target("/e2_disjoint_prefetch")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e3s.name")
                .queryParam("cayenneExp", "id < 3")
                .queryParam("sort", "id")
                .request().get();

        onSuccess(r)
                .bodyEquals(2, "{\"id\":1,\"e3s\":[{\"name\":\"yyy\"}],\"name\":\"xxx\"},{\"id\":2,\"e3s\":[],\"name\":\"aaa\"}")
                // disjoint prefetch is counted as 1 query at the DataDomainFilter level
                .ranQueries(1);
    }

    @Test
    public void test_QueryWithParentIdsResolver() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "aaa").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        Response r = target("/e2_query_with_parent_ids")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e3s.name")
                .queryParam("cayenneExp", "id < 3")
                .queryParam("sort", "id")
                .request().get();

        onSuccess(r)
                .bodyEquals(2, "{\"id\":1,\"e3s\":[{\"name\":\"yyy\"}],\"name\":\"xxx\"},{\"id\":2,\"e3s\":[],\"name\":\"aaa\"}")
                .ranQueries(2);
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2_disjoint_prefetch")
        public DataResponse<E2> e2_disjoint_prefetch(@Context UriInfo uriInfo) {

            // non-standard nested resolver
            AgEntityOverlay<E2> e2Overlay = AgEntity
                    .overlay(E2.class)
                    .redefineRelationshipResolver("e3s", CayenneResolvers.nestedViaDisjointParentPrefetch());

            return Ag.select(E2.class, config)
                    .entityOverlay(e2Overlay)
                    .uri(uriInfo)
                    .get();
        }

        @GET
        @Path("e2_joint_prefetch")
        public DataResponse<E2> e2_joint_prefetch(@Context UriInfo uriInfo) {

            // non-standard nested resolver
            AgEntityOverlay<E2> e2Overlay = AgEntity
                    .overlay(E2.class)
                    .redefineRelationshipResolver("e3s", CayenneResolvers.nestedViaJointParentPrefetch());

            return Ag.select(E2.class, config)
                    .entityOverlay(e2Overlay)
                    .uri(uriInfo)
                    .get();
        }

        @GET
        @Path("e2_query_with_parent_ids")
        public DataResponse<E2> e2_query_with_parent_ids(@Context UriInfo uriInfo) {

            // non-standard nested resolver
            AgEntityOverlay<E2> e2Overlay = AgEntity
                    .overlay(E2.class)
                    .redefineRelationshipResolver("e3s", CayenneResolvers.nestedViaQueryWithParentIds(config));

            return Ag.select(E2.class, config)
                    .entityOverlay(e2Overlay)
                    .uri(uriInfo)
                    .get();
        }
    }
}
