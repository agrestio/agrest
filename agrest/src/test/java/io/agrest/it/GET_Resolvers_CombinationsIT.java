package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E15;
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

    private OverlayType o1;
    private OverlayType o2;
    private int queryCount;

    public GET_Resolvers_CombinationsIT(OverlayType o1, OverlayType o2, int queryCount) {
        this.o1 = o1;
        this.o2 = o2;
        this.queryCount = queryCount;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return asList(
                new Object[][]{

                        // unique pairs of overlay types
                        {OverlayType.joint, OverlayType.parentExp, 2},
                        {OverlayType.joint, OverlayType.parentId, 2},
                        {OverlayType.parentExp, OverlayType.parentId, 3},

                        // unique pairs - reversed
                        {OverlayType.parentExp, OverlayType.joint, 2},
                        {OverlayType.parentId, OverlayType.joint, 2},
                        {OverlayType.parentId, OverlayType.parentExp, 3},

                        // paired with self
                        {OverlayType.joint, OverlayType.joint, 1},
                        {OverlayType.parentExp, OverlayType.parentExp, 3},
                        {OverlayType.parentId, OverlayType.parentId, 3}
                });
    }

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntitiesAndDependencies() {
        return new Class[]{E15.class};
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E5.class};
    }

    @Override
    protected CayenneTestDataManager createDataManager(BQRuntime runtime) {
        return CayenneTestDataManager.builder(TEST_RUNTIME)
                .entities(testEntities())
                .entitiesAndDependencies(testEntitiesAndDependencies())
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
        e15_5().deleteAll();
        e5().deleteAll();
        e2().deleteAll();
        e15().deleteAll();

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

        e15().insertColumns("long_id", "name")
                .values(1L, "e15_1")
                .values(2L, "e15_2")
                .values(3L, "e15_3")
                .exec();

        e15_5().insertColumns("e15_id", "e5_id")
                .values(1L, 1)
                .values(2L, 3)
                .values(3L, 3)
                .exec();

        dataLoaded = true;
    }

    @Test
    public void test_ToManyToOne() {

        Response r = target("/tomany_toone")
                .queryParam("include", "id")
                .queryParam("include", "e3s.name")
                .queryParam("include", "e3s.e2.name")
                .queryParam("cayenneExp", "id < 3")
                .queryParam("sort", "id")
                .queryParam("o1", o1)
                .queryParam("o2", o2)
                .request().get();

        onSuccess(r)
                .bodyEquals(2,
                        "{\"id\":1,\"e3s\":[{\"e2\":{\"name\":\"e2_2\"},\"name\":\"e3_1\"}]}," +
                                "{\"id\":2,\"e3s\":[{\"e2\":null,\"name\":\"e3_2\"}]}")
                .ranQueries(queryCount);
    }

    @Test
    public void test_ToOneToMany() {

        Response r = target("/toone_tomany")
                .queryParam("include", "id")
                .queryParam("include", "e5.name")
                .queryParam("include", "e5.e15s.name")
                .queryParam("cayenneExp", "id < 30")
                .queryParam("sort", "id")
                .queryParam("o1", o1)
                .queryParam("o2", o2)
                .request().get();

        onSuccess(r)
                .bodyEquals(2,
                        "{\"id\":11,\"e5\":{\"e15s\":[],\"name\":\"e5_2\"}}," +
                                "{\"id\":13,\"e5\":{\"e15s\":[{\"name\":\"e15_2\"},{\"name\":\"e15_3\"}],\"name\":\"e5_3\"}}")
                .ranQueries(queryCount);
    }

    public enum OverlayType {
        joint, parentExp, parentId;
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("tomany_toone")
        public DataResponse<E5> tomany_toone(
                @QueryParam("o1") OverlayType e5o,
                @QueryParam("o2") OverlayType e3o,
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

        @GET
        @Path("toone_tomany")
        public DataResponse<E3> toone_tomany(
                @QueryParam("o1") OverlayType e5o,
                @QueryParam("o2") OverlayType e3o,
                @Context UriInfo uriInfo) {

            AgEntityOverlay<E3> o1 = AgEntity
                    .overlay(E3.class)
                    .redefineRelationshipResolver(E3.E5.getName(), resolverFactory(e3o));

            AgEntityOverlay<E5> o2 = AgEntity
                    .overlay(E5.class)
                    .redefineRelationshipResolver(E5.E15S.getName(), resolverFactory(e5o));

            return Ag.select(E3.class, config)
                    .entityOverlay(o1)
                    .entityOverlay(o2)
                    .uri(uriInfo)
                    .get();
        }

        NestedDataResolverFactory resolverFactory(OverlayType o) {
            switch (o) {
                case joint:
                    return CayenneResolvers.nested(config).viaParentPrefetch();
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
