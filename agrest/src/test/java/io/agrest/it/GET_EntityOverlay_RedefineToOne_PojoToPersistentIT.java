package io.agrest.it;

import io.agrest.Ag;
import io.agrest.AgException;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E25;
import io.agrest.it.fixture.pojox.PX1;
import io.agrest.it.fixture.pojox.PX1RootResolver;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.runtime.cayenne.CayenneResolvers;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class GET_EntityOverlay_RedefineToOne_PojoToPersistentIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E25.class};
    }

    @Test
    public void testFailWithCayenneResolver_nestedViaQueryWithParentIds() {

        e25().insertColumns("id")
                .values(1)
                .values(2)
                .values(3)
                .values(4).exec();

        Response r = target("/pojo_to_cayenne_cant_use_nestedViaQueryWithParentIds")
                .queryParam("include", "id")
                .queryParam("include", "overlayToOne")
                .queryParam("sort", "id")
                .request()
                .get();

        onResponse(r)
                .statusEquals(Response.Status.INTERNAL_SERVER_ERROR)
                .bodyEquals("{\"success\":false,\"message\":" +
                        "\"_Entity 'PX1' is not mapped in Cayenne, so its child 'E25' can't be resolved with io.agrest.runtime.cayenne.processor.select.ViaQueryWithParentIdsResolver\"}");
    }

    @Test
    public void testFailWithCayenneResolver_nestedViaQueryWithParentExp() {

        e25().insertColumns("id")
                .values(1)
                .values(2)
                .values(3)
                .values(4).exec();

        Response r = target("/pojo_to_cayenne_cant_use_nestedViaQueryWithParentExp")
                .queryParam("include", "id")
                .queryParam("include", "overlayToOne")
                .queryParam("sort", "id")
                .request()
                .get();

        onResponse(r)
                .statusEquals(Response.Status.INTERNAL_SERVER_ERROR)
                .bodyEquals("{\"success\":false,\"message\":" +
                        "\"_Entity 'PX1' is not mapped in Cayenne, so its child 'E25' can't be resolved with io.agrest.runtime.cayenne.processor.select.ViaQueryWithParentExpResolver\"}");
    }

    @Test
    public void testFailWithCayenneResolver_nestedViaJointParentPrefetch() {

        e25().insertColumns("id")
                .values(1)
                .values(2)
                .values(3)
                .values(4).exec();

        Response r = target("/pojo_to_cayenne_cant_use_nestedViaJointParentPrefetch")
                .queryParam("include", "id")
                .queryParam("include", "overlayToOne")
                .queryParam("sort", "id")
                .request()
                .get();

        onResponse(r)
                .statusEquals(Response.Status.INTERNAL_SERVER_ERROR)
                .bodyEquals("{\"success\":false,\"message\":" +
                        "\"_Can't add prefetch to root entity that has no SelectQuery of its own. Path: overlayToOne\"}");
    }

    @Path("")
    public static final class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("pojo_to_cayenne_cant_use_nestedViaQueryWithParentIds")
        public DataResponse<PX1> pojo_to_cayenne_cant_use_nestedViaQueryWithParentIds(@Context UriInfo uriInfo) {

            AgEntityOverlay<PX1> o1 = AgEntity
                    .overlay(PX1.class)
                    .redefineRootDataResolver(new PX1RootResolver())
                    // Cayenne resolver will fail as the parent is not a Cayenne object
                    .redefineToOne("overlayToOne", E25.class, CayenneResolvers.nestedViaQueryWithParentIds(config));

            try {
                return Ag.service(config)
                        .select(PX1.class)
                        .uri(uriInfo)
                        .entityOverlay(o1)
                        .get();
            } catch (IllegalStateException e) {
                // propagate internal exception to the caller so it can make assertions
                throw new AgException(Response.Status.INTERNAL_SERVER_ERROR, "_" + e.getMessage());
            }
        }

        @GET
        @Path("pojo_to_cayenne_cant_use_nestedViaQueryWithParentExp")
        public DataResponse<PX1> pojo_to_cayenne_cant_use_nestedViaQueryWithParentExp(@Context UriInfo uriInfo) {

            AgEntityOverlay<PX1> o1 = AgEntity
                    .overlay(PX1.class)
                    .redefineRootDataResolver(new PX1RootResolver())
                    // Cayenne resolver will fail as the parent is not a Cayenne object
                    .redefineToOne("overlayToOne", E25.class, CayenneResolvers.nestedViaQueryWithParentExp(config));

            try {
                return Ag.service(config)
                        .select(PX1.class)
                        .uri(uriInfo)
                        .entityOverlay(o1)
                        .get();
            } catch (IllegalStateException e) {
                // propagate internal exception to the caller so it can make assertions
                throw new AgException(Response.Status.INTERNAL_SERVER_ERROR, "_" + e.getMessage());
            }
        }

        @GET
        @Path("pojo_to_cayenne_cant_use_nestedViaJointParentPrefetch")
        public DataResponse<PX1> pojo_to_cayenne_cant_use_nestedViaJointParentPrefetch(@Context UriInfo uriInfo) {

            AgEntityOverlay<PX1> o1 = AgEntity
                    .overlay(PX1.class)
                    .redefineRootDataResolver(new PX1RootResolver())
                    // Cayenne resolver will fail as the parent is not a Cayenne object
                    .redefineToOne("overlayToOne", E25.class, CayenneResolvers.nestedViaJointParentPrefetch());

            try {
                return Ag.service(config)
                        .select(PX1.class)
                        .uri(uriInfo)
                        .entityOverlay(o1)
                        .get();
            } catch (IllegalStateException e) {
                // propagate internal exception to the caller so it can make assertions
                throw new AgException(Response.Status.INTERNAL_SERVER_ERROR, "_" + e.getMessage());
            }
        }
    }
}
