package io.agrest.cayenne.GET;

import io.agrest.AgException;
import io.agrest.AgResponse;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.CayenneResolvers;
import io.agrest.cayenne.cayenne.main.E25;
import io.agrest.cayenne.pojo.model.PX1;
import io.agrest.cayenne.pojo.runtime.PX1RootResolver;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.resolver.RelatedDataResolverFactory;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

public class Resolvers_PojoToPersistentIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class).entities(E25.class).build();

    @ParameterizedTest
    @EnumSource(Resolvers_CombinationsIT.Overlay.class)
    public void fail(Resolvers_CombinationsIT.Overlay overlay) {

        tester.target("/px1")
                .queryParam("overlay", overlay)
                .queryParam("include", "e25")
                .get()
                .wasServerError()
                .bodyEquals("{\"message\":\"Entity 'PX1' is not mapped in Cayenne, so its relationship 'e25' can't be resolved with a Cayenne resolver\"}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("px1")
        public AgResponse px1(
                @QueryParam("overlay") Resolvers_CombinationsIT.Overlay overlay,
                @Context UriInfo uriInfo) {

            AgEntityOverlay<PX1> px1Overlay = AgEntity
                    .overlay(PX1.class)
                    .dataResolver(new PX1RootResolver())
                    // Cayenne resolver will fail as the parent is not a Cayenne object
                    .toOne("e25", E25.class, resolverFactory(overlay));

            try {
                return AgJaxrs.select(PX1.class, config)
                        .entityOverlay(px1Overlay)
                        .clientParams(uriInfo.getQueryParameters())
                        .get();
            } catch (AgException e) {

                // intentionally leaking the underlying exception message to the client, so we can make an assertion
                return SimpleResponse.of(500, e.getCause().getMessage());
            }
        }

        RelatedDataResolverFactory resolverFactory(Resolvers_CombinationsIT.Overlay o) {
            switch (o) {
                case joint:
                    return CayenneResolvers.relatedViaParentPrefetch();
                case parentExp:
                    return CayenneResolvers.relatedViaQueryWithParentExp();
                case parentId:
                    return CayenneResolvers.relatedViaQueryWithParentIds();
                default:
                    throw new IllegalStateException("?");
            }
        }
    }
}
