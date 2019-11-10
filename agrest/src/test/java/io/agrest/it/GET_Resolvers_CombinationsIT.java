package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E5;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.resolver.NestedDataResolverFactory;
import io.agrest.runtime.cayenne.CayenneResolvers;
import io.bootique.BQRuntime;
import io.bootique.cayenne.test.CayenneTestDataManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

import static java.util.Arrays.asList;

@RunWith(Parameterized.class)
public class GET_Resolvers_CombinationsIT extends JerseyAndDerbyCase {

    private static boolean dataLoaded;

    private OverlayType e5o;
    private OverlayType e3o;
    private int queryCount;

    public GET_Resolvers_CombinationsIT(OverlayType e5o, OverlayType e3o, int queryCount) {
        this.e5o = e5o;
        this.e3o = e3o;
        this.queryCount = queryCount;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return asList(
                new Object[][]{

                        // note that "disjoint" prefetches are batched together with their parent query with out approach
                        // to query counting using DataChannelFilter. We can't meter them as individual queries yet.

                        // unique pairs of overlay types
                        {OverlayType.joint, OverlayType.disjoint, 1},
                        {OverlayType.joint, OverlayType.parentExp, 2},
                        {OverlayType.joint, OverlayType.parentId, 2},
                        {OverlayType.disjoint, OverlayType.parentExp, 2},
                        {OverlayType.disjoint, OverlayType.parentId, 2},
                        {OverlayType.parentExp, OverlayType.parentId, 3},

                        // unique pairs - reversed
                        {OverlayType.disjoint, OverlayType.joint, 1},
                        {OverlayType.parentExp, OverlayType.joint, 2},
                        {OverlayType.parentId, OverlayType.joint, 2},
                        {OverlayType.parentExp, OverlayType.disjoint, 2},
                        {OverlayType.parentId, OverlayType.disjoint, 2},
                        {OverlayType.parentId, OverlayType.parentExp, 3}
                });
    }

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E5.class};
    }

    @Override
    protected CayenneTestDataManager createDataManager(BQRuntime runtime) {
        return CayenneTestDataManager.builder(TEST_RUNTIME)
                .entities(testEntities())
                // avoid data deletion... we reuse the dataset after creating it once
                .doNotDeleteData()
                .build();
    }

    @Before
    public void loadData() {
        if (dataLoaded) {
            return;
        }

        // manually managing deletes as test data manager is configured to avoid deletes
        e3().deleteAll();
        e5().deleteAll();
        e2().deleteAll();

        e5().insertColumns("id", "name")
                .values(1, "e5_1")
                .values(2, "e5_2")
                .values(3, "e5_3").exec();

        e2().insertColumns("id_", "name")
                .values(1, "e2_1")
                .values(2, "e2_2").exec();

        e3().insertColumns("id_", "name", "e5_id", "e2_id")
                .values(34, "e3_1", 1, 2)
                .values(11, "e3_2", 2, null)
                .values(13, "e3_3", 3, 1)
                .exec();

        dataLoaded = true;
    }

    @Test
    public void test() {

        Response r = target("/")
                .queryParam("include", "id")
                .queryParam("include", "e3s.name")
                .queryParam("include", "e3s.e2.name")
                .queryParam("cayenneExp", "id < 3")
                .queryParam("sort", "id")
                .queryParam("e5o", e5o)
                .queryParam("e3o", e3o)
                .request().get();

        onSuccess(r)
                .bodyEquals(2,
                        "{\"id\":1,\"e3s\":[{\"e2\":{\"name\":\"e2_2\"},\"name\":\"e3_1\"}]}," +
                                "{\"id\":2,\"e3s\":[{\"e2\":null,\"name\":\"e3_2\"}]}")
                .ranQueries(queryCount);
    }

    public enum OverlayType {
        joint, disjoint, parentExp, parentId;
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        public DataResponse<E5> get(
                @QueryParam("e5o") OverlayType e5o,
                @QueryParam("e3o") OverlayType e3o,
                @Context UriInfo uriInfo) {

            AgEntityOverlay<E5> o1 = AgEntity
                    .overlay(E5.class)
                    .redefineRelationshipResolver(E5.E3S.getName(), resolverFactory(e5o));

            AgEntityOverlay<E3> o2 = AgEntity
                    .overlay(E3.class)
                    .redefineRelationshipResolver(E3.E2.getName(), resolverFactory(e3o));

            return Ag.select(E5.class, config)
                    .entityOverlay(o1)
                    .entityOverlay(o2)
                    .uri(uriInfo)
                    .get();
        }

        NestedDataResolverFactory resolverFactory(OverlayType o) {
            switch (o) {
                case joint:
                    return CayenneResolvers.nested(config).viaJointParentPrefetch();
                case disjoint:
                    return CayenneResolvers.nested(config).viaDisjointParentPrefetch();
                case parentExp:
                    return CayenneResolvers.nested(config).viaQueryWithParentExp();
                case parentId:
                    return CayenneResolvers.nested(config).viaQueryWithParentIds();
                default:
                    throw new IllegalStateException("?");
            }
        }
    }
}
