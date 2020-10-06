package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E5;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.unit.CayenneAgTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.runtime.AgRuntime;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SelectById;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

public class GET_Resolvers_MixedIT extends DbTest {

    @BQTestTool
    static final CayenneAgTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E5.class)
            .build();

    @Test
    public void test_alt_resolver__parentids_joint_prefetch() {

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

        tester.target("/test_alt_resolver__parentids_joint_prefetch")
                .queryParam("include", "id")
                .queryParam("include", "e3s.name")
                .queryParam("include", "e3s.e2.name")
                .queryParam("cayenneExp", "id < 3")
                .queryParam("sort", "id")
                .get()
                .wasSuccess()
                .bodyEquals(2,
                        "{\"id\":1,\"e3s\":[{\"e2\":{\"name\":\"e2_2\"},\"name\":\"e3_1\"}]}",
                        "{\"id\":2,\"e3s\":[{\"e2\":null,\"name\":\"e3_2\"}]}");

        tester.assertQueryCount(2);
    }

    @Test
    public void test_alt_mix_up_relations() {

        tester.e5().insertColumns("id", "name")
                .values(1, "e5_1")
                .values(2, "e5_2")
                .values(3, "e5_3").exec();

        tester.e2().insertColumns("id_", "name")
                .values(1, "e2_1")
                .values(2, "e2_2").exec();

        tester.e3().insertColumns("id_", "name")
                .values(2, "e3_2")
                .values(1, "e3_1")
                .values(3, "e3_3")
                .exec();

        tester.target("/test_mix_up_relations")
                .queryParam("include", "id")
                .queryParam("include", "e3s.name")
                .queryParam("include", "e3s.ex.name")
                .queryParam("cayenneExp", "id < 3")
                .queryParam("sort", "id")
                .get()
                .wasSuccess()
                .bodyEquals(2,
                        "{\"id\":1,\"e3s\":[{\"ex\":{\"name\":\"e3_2\"},\"name\":\"e2_2\"}]}",
                        "{\"id\":2,\"e3s\":[]}");

        tester.assertQueryCount(4);
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("test_alt_resolver__parentids_joint_prefetch")
        public DataResponse<E5> test_alt_resolver__parentids_joint_prefetch(@Context UriInfo uriInfo) {

            AgEntityOverlay<E5> o1 = AgEntity
                    .overlay(E5.class)
                    .redefineRelationshipResolver(E5.E3S.getName(), CayenneResolvers.nested(config).viaQueryWithParentIds());

            AgEntityOverlay<E3> o2 = AgEntity
                    .overlay(E3.class)
                    .redefineRelationshipResolver(E3.E2.getName(), CayenneResolvers.nested(config).viaParentPrefetch());

            return Ag.select(E5.class, config)
                    .entityOverlay(o1)
                    .entityOverlay(o2)
                    .uri(uriInfo)
                    .get();
        }

        @GET
        @Path("test_mix_up_relations")
        public DataResponse<E5> test_mix_up_relations(@Context UriInfo uriInfo) {

            ObjectContext context = AgRuntime.service(ICayennePersister.class, config).sharedContext();

            AgEntityOverlay<E5> o1 = AgEntity
                    .overlay(E5.class)
                    .redefineToMany(E5.E3S.getName(), E2.class, e5 ->
                            ObjectSelect.query(E2.class)
                                    .where(ExpressionFactory.greaterDbExp(E2.ID__PK_COLUMN, Cayenne.longPKForObject(e5)))
                                    .select(context)
                    );

            AgEntityOverlay<E2> o2 = AgEntity
                    .overlay(E2.class)
                    .redefineToOne("ex", E3.class, e2 ->
                            SelectById.query(E3.class, Cayenne.intPKForObject(e2)).selectOne(context)
                    );

            return Ag.select(E5.class, config)
                    .entityOverlay(o1)
                    .entityOverlay(o2)
                    .uri(uriInfo)
                    .get();
        }
    }
}
