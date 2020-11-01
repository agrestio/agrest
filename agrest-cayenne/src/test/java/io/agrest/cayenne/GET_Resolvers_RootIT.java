package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.auto._E2;
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

public class GET_Resolvers_RootIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class)
            .build();

    @Test
    public void test_ViaQueryResolver() {

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
    public void test_ViaCustomResolver() {

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

            // non-standard nested resolver
            AgEntityOverlay<E2> e2Overlay = AgEntity
                    .overlay(E2.class)
                    // this is what Ag uses by default, but let's see if it still works as an override
                    .redefineRootDataResolver(CayenneResolvers.root(config).viaQuery())
                    // check how a combination of custom root and nested resolvers works
                    .redefineRelationshipResolver("e3s", CayenneResolvers.nested(config).viaParentPrefetch());

            return Ag.select(E2.class, config)
                    .entityOverlay(e2Overlay)
                    .uri(uriInfo)
                    .get();
        }

        @GET
        @Path("e2_custom_query")
        public DataResponse<E2> e2_custom_query(@Context UriInfo uriInfo) {

            // non-standard nested resolver
            AgEntityOverlay<E2> e2Overlay = AgEntity
                    .overlay(E2.class)
                    .redefineRootDataResolver(new CustomE2Resolver())
                    // check how a combination of custom root and Cayenne nested resolvers works
                    .redefineRelationshipResolver("e3s", CayenneResolvers.nested(config).viaQueryWithParentIds());

            return Ag.select(E2.class, config)
                    .entityOverlay(e2Overlay)
                    .uri(uriInfo)
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
