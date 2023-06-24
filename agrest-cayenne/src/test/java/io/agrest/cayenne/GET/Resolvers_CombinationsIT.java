package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.CayenneResolvers;
import io.agrest.cayenne.cayenne.main.E15;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E5;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.resolver.RelatedDataResolverFactory;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.stream.Stream;

public class Resolvers_CombinationsIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E5.class)
            .entitiesAndDependencies(E15.class)
            .build();

    @BeforeEach
    void loadData() {

        tester.e5().insertColumns("id", "name")
                .values(1, "e5_1")
                .values(2, "e5_2")
                .values(3, "e5_3").exec();

        tester.e2().insertColumns("id_", "name")
                .values(1, "e2_1")
                .values(2, "e2_2").exec();

        tester.e3().insertColumns("id_", "name", "e5_id", "e2_id")
                .values(34, "e3_1", 1, 2)
                .values(11, "e3_2", 2, null)
                .values(13, "e3_3", 3, 1)
                .exec();

        tester.e15().insertColumns("long_id", "name")
                .values(1L, "e15_1")
                .values(2L, "e15_2")
                .values(3L, "e15_3")
                .exec();

        tester.e15_5().insertColumns("e15_id", "e5_id")
                .values(1L, 1)
                .values(2L, 3)
                .values(3L, 3)
                .exec();
    }

    private static Stream<Arguments> provideTestInputs() {
        return Stream.of(

                // unique pairs of overlay types
                Arguments.of(Overlay.joint, Overlay.parentExp, 2),
                Arguments.of(Overlay.joint, Overlay.parentId, 2),
                Arguments.of(Overlay.parentExp, Overlay.parentId, 3),

                // unique pairs - reversed
                Arguments.of(Overlay.parentExp, Overlay.joint, 2),
                Arguments.of(Overlay.parentId, Overlay.joint, 2),
                Arguments.of(Overlay.parentId, Overlay.parentExp, 3),

                // paired with self
                Arguments.of(Overlay.joint, Overlay.joint, 1),
                Arguments.of(Overlay.parentExp, Overlay.parentExp, 3),
                Arguments.of(Overlay.parentId, Overlay.parentId, 3)
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestInputs")
    public void toManyToOne(Overlay o1, Overlay o2, int queryCount) {

        tester.target("/tomany_toone")
                .queryParam("include", "id")
                .queryParam("include", "e3s.name")
                .queryParam("include", "e3s.e2.name")
                .queryParam("exp", "id < 3")
                .queryParam("sort", "id")
                .queryParam("o1", o1)
                .queryParam("o2", o2)
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":1,\"e3s\":[{\"e2\":{\"name\":\"e2_2\"},\"name\":\"e3_1\"}]}",
                        "{\"id\":2,\"e3s\":[{\"e2\":null,\"name\":\"e3_2\"}]}");

        tester.assertQueryCount(queryCount);
    }

    @ParameterizedTest
    @MethodSource("provideTestInputs")
    public void toOneToMany(Overlay o1, Overlay o2, int queryCount) {

        tester.target("/toone_tomany")
                .queryParam("include", "id")
                .queryParam("include", "e5.name")
                .queryParam("include", "e5.e15s.name")
                .queryParam("exp", "id < 30")
                .queryParam("sort", "id")
                .queryParam("o1", o1)
                .queryParam("o2", o2)
                .get().wasOk()
                .bodyEquals(2,
                        "{\"id\":11,\"e5\":{\"e15s\":[],\"name\":\"e5_2\"}}",
                        "{\"id\":13,\"e5\":{\"e15s\":[{\"name\":\"e15_2\"},{\"name\":\"e15_3\"}],\"name\":\"e5_3\"}}");

        tester.assertQueryCount(queryCount);
    }

    public enum Overlay {
        joint, parentExp, parentId
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("tomany_toone")
        public DataResponse<E5> tomany_toone(
                @QueryParam("o1") Overlay e5o,
                @QueryParam("o2") Overlay e3o,
                @Context UriInfo uriInfo) {

            AgEntityOverlay<E5> o1 = AgEntity
                    .overlay(E5.class)
                    .relatedDataResolver(E5.E3S.getName(), resolverFactory(e5o));

            AgEntityOverlay<E3> o2 = AgEntity
                    .overlay(E3.class)
                    .relatedDataResolver(E3.E2.getName(), resolverFactory(e3o));

            return AgJaxrs.select(E5.class, config)
                    .entityOverlay(o1)
                    .entityOverlay(o2)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }

        @GET
        @Path("toone_tomany")
        public DataResponse<E3> toone_tomany(
                @QueryParam("o1") Overlay e5o,
                @QueryParam("o2") Overlay e3o,
                @Context UriInfo uriInfo) {

            AgEntityOverlay<E3> o1 = AgEntity
                    .overlay(E3.class)
                    .relatedDataResolver(E3.E5.getName(), resolverFactory(e3o));

            AgEntityOverlay<E5> o2 = AgEntity
                    .overlay(E5.class)
                    .relatedDataResolver(E5.E15S.getName(), resolverFactory(e5o));

            return AgJaxrs.select(E3.class, config)
                    .entityOverlay(o1)
                    .entityOverlay(o2)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }

        RelatedDataResolverFactory resolverFactory(Overlay o) {
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
