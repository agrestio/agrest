package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.CayenneResolvers;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.auto._E2;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.resolver.BaseRootDataResolver;
import io.agrest.runtime.processor.select.SelectContext;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.ObjectId;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static java.util.Arrays.asList;

public class GET_Resolvers_RootIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class)
            .build();

    @Test
    public void viaQueryResolver() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "aaa").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        tester.target("/e2_standard_query")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e3s.name")
                .queryParam("exp", "id < 3")
                .queryParam("sort", "id")
                .get().wasOk()
                .bodyEquals(2,
                        "{\"id\":1,\"e3s\":[{\"name\":\"yyy\"}],\"name\":\"xxx\"}",
                        "{\"id\":2,\"e3s\":[],\"name\":\"aaa\"}");

        tester.assertQueryCount(1);
    }

    @Test
    public void viaCustomResolver() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "aaa").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        tester.target("/e2_custom_query")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e3s.name")
                .get().wasOk()
                .bodyEquals(2,
                        "{\"id\":2,\"e3s\":[],\"name\":\"n_2\"}",
                        "{\"id\":1,\"e3s\":[{\"name\":\"yyy\"}],\"name\":\"n_1\"}");

        tester.assertQueryCount(1);
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2_standard_query")
        public DataResponse<E2> e2_standard_query(@Context UriInfo uriInfo) {

            // non-standard related resolver
            AgEntityOverlay<E2> e2Overlay = AgEntity
                    .overlay(E2.class)
                    // this is what Ag uses by default, but let's see if it still works as an override
                    .dataResolverFactory(CayenneResolvers.rootViaQuery())
                    // check how a combination of custom root and related resolvers works
                    .relatedDataResolver("e3s", CayenneResolvers.relatedViaParentPrefetch());

            return AgJaxrs.select(E2.class, config)
                    .entityOverlay(e2Overlay)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }

        @GET
        @Path("e2_custom_query")
        public DataResponse<E2> e2_custom_query(@Context UriInfo uriInfo) {

            // non-standard related resolver
            AgEntityOverlay<E2> e2Overlay = AgEntity
                    .overlay(E2.class)
                    .dataResolver(new CustomE2Resolver())
                    // check how a combination of custom root and Cayenne related resolvers works
                    .relatedDataResolver("e3s", CayenneResolvers.relatedViaQueryWithParentIds());

            return AgJaxrs.select(E2.class, config)
                    .entityOverlay(e2Overlay)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }
    }

    static class CustomE2Resolver extends BaseRootDataResolver<E2> {

        @Override
        protected void doAssembleQuery(SelectContext<E2> context) {
            // do nothing...
        }

        @Override
        protected List<E2> doFetchData(SelectContext<E2> context) {
            return asList(e2(2), e2(1));
        }

        private E2 e2(int i) {
            E2 e2 = new E2();
            e2.setName("n_" + i);
            e2.setObjectId(ObjectId.of("e2", _E2.ID__PK_COLUMN, i));

            return e2;
        }
    }
}
