package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.runtime.cayenne.AgCayenne;
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

import static org.junit.Assert.*;

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

        assertEquals(0, cayenneOpCounter.getQueryCounter());
        Response r = target("/e3_joint_prefetch")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e2.name")
                .queryParam("cayenneExp", "id > 3")
                .request().get();

        assertEquals(1, cayenneOpCounter.getQueryCounter());

        onSuccess(r).bodyEquals(2, "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\"},{\"id\":9,\"e2\":null,\"name\":\"zzz\"}");
    }

    @Test
    public void test_DisjointPrefetchResolver() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        assertEquals(0, cayenneOpCounter.getQueryCounter());
        Response r = target("/e3_disjoint_prefetch")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e2.name")
                .queryParam("cayenneExp", "id > 3")
                .request().get();

        // disjoint prefetch is counted as 1 query at the DataDomainFilter level
        assertEquals(1, cayenneOpCounter.getQueryCounter());

        onSuccess(r).bodyEquals(2, "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\"},{\"id\":9,\"e2\":null,\"name\":\"zzz\"}");
    }

    @Test
    public void test_QueryWithParentIdsResolver() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        assertEquals(0, cayenneOpCounter.getQueryCounter());
        Response r = target("/e3_query_with_parent_ids")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e2.name")
                .queryParam("cayenneExp", "id > 3")
                .request().get();

        assertEquals(2, cayenneOpCounter.getQueryCounter());

        onSuccess(r).bodyEquals(2, "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\"},{\"id\":9,\"e2\":null,\"name\":\"zzz\"}");
    }

    @Test
    public void test_QueryWithParentQualifierResolver() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        assertEquals(0, cayenneOpCounter.getQueryCounter());
        Response r = target("/e3_query_with_parent_qualifier")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e2.name")
                .queryParam("cayenneExp", "id > 3")
                .request().get();

        assertEquals(2, cayenneOpCounter.getQueryCounter());

        onSuccess(r).bodyEquals(2, "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\"},{\"id\":9,\"e2\":null,\"name\":\"zzz\"}");
    }

    @Test
    public void test_QueryWithParentQualifierResolver_NoFetchIfNoParent() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        assertEquals(0, cayenneOpCounter.getQueryCounter());
        Response r = target("/e3_query_with_parent_qualifier")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e2.name")
                .queryParam("cayenneExp", "id > 9")
                .request().get();

        assertEquals(1, cayenneOpCounter.getQueryCounter());

        onSuccess(r).bodyEquals(0, "");
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
                    .redefineRelationshipResolver("e2", AgCayenne.resolverViaJointParentPrefetch(config));

            return Ag.select(E3.class, config)
                    .entityOverlay(e3Overlay)
                    .uri(uriInfo)
                    .get();
        }

        @GET
        @Path("e3_disjoint_prefetch")
        public DataResponse<E3> e3_disjoint_prefetch(@Context UriInfo uriInfo) {

            // non-standard nested resolver
            AgEntityOverlay<E3> e3Overlay = AgEntity
                    .overlay(E3.class)
                    .redefineRelationshipResolver("e2", AgCayenne.resolverViaDisjointParentPrefetch(config));

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
                    .redefineRelationshipResolver("e2", AgCayenne.resolverViaQueryWithParentIds(config));

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
                    .redefineRelationshipResolver("e2", AgCayenne.resolverViaQueryWithParentQualifier(config));

            return Ag.select(E3.class, config)
                    .entityOverlay(e3Overlay)
                    .uri(uriInfo)
                    .get();
        }
    }
}
