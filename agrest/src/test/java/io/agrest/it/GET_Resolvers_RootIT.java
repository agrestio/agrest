package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.auto._E2;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.resolver.BaseRootDataResolver;
import io.agrest.runtime.cayenne.CayenneResolvers;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.ObjectId;
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
import java.util.List;

import static java.util.Arrays.asList;

public class GET_Resolvers_RootIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class};
    }

    @Test
    public void test_ViaQueryResolver() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "aaa").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        Response r = target("/e2_standard_query")
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
    public void test_ViaCustomResolver() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "aaa").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null)
                .exec();

        Response r = target("/e2_custom_query")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", "e3s.name")
                .request().get();

        onSuccess(r)
                .bodyEquals(2, "{\"id\":2,\"e3s\":[],\"name\":\"n_2\"},{\"id\":1,\"e3s\":[{\"name\":\"yyy\"}],\"name\":\"n_1\"}")
                .ranQueries(1);
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
                    .redefineRelationshipResolver("e3s", CayenneResolvers.nested(config).viaJointParentPrefetch());

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
            e2.setObjectId(new ObjectId("e2", _E2.ID__PK_COLUMN, i));

            return e2;
        }
    }
}
