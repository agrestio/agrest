package io.agrest.sencha;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.sencha.ops.unit.SenchaBodyAssertions;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

public class Sencha_PUT_IT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class)
            .build();

    @Test
    public void testPut_ToOne_FromNull() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", null).exec();

        tester.target("/e3/3").put("{\"id\":3,\"e2_id\":8}")
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("id_", 3).eq("e2_id", 8).assertOneMatch();
    }

    @Test
    public void testPut_ToOne_ToNull() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        tester.target("/e3/3").put("{\"id\":3,\"e2_id\":null}")
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().assertOneMatch();
        tester.e3().matcher().eq("id_", 3).eq("e2_id", null).assertOneMatch();
    }

    @Test
    public void testPut_ToOne() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        tester.target("/e3/3").put("{\"id\":3,\"e2_id\":1}")
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("id_", 3).eq("e2_id", 1).assertOneMatch();
    }

    @Test
    public void testPut_ToOne_Relationship_Name() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(3, "zzz", 8).exec();

        tester.target("/e3/3").put("{\"id\":3,\"e2\":1}")
                .wasOk()
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("id_", 3).eq("e2_id", 1).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e3/{id}")
        public DataResponse<E3> updateE3(@PathParam("id") int id, String requestBody) {
            return Ag.update(E3.class, config).id(id).syncAndSelect(requestBody);
        }
    }
}
