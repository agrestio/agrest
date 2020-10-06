package io.agrest.sencha;

import io.agrest.Ag;
import io.agrest.EntityDelete;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E17;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.unit.CayenneAgTester;
import io.agrest.cayenne.unit.DbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

public class Sencha_DELETE_IT extends DbTest {

    @BQTestTool
    static final CayenneAgTester tester = tester(E2Resource.class, E17Resource.class)
            .entitiesAndDependencies(E2.class, E17.class)
            .build();

    @Test
    public void test_BatchDelete() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        tester.target("/e2").deleteWithEntity("[{\"id\":1},{\"id\":2}]").wasSuccess();

        tester.e2().matcher().assertOneMatch();
        tester.e2().matcher().eq("id_", 3).assertOneMatch();
    }

    @Test
    public void test_BatchDelete_CompoundId() {

        tester.e17().insertColumns("id1", "id2", "name")
                .values(1, 1, "aaa")
                .values(2, 2, "bbb")
                .values(3, 3, "ccc").exec();

        tester.target("/e17").deleteWithEntity("[{\"id1\":1,\"id2\":1},{\"id1\":2,\"id2\":2}]").wasSuccess();

        tester.e17().matcher().assertOneMatch();
        tester.e17().matcher().eq("id1", 3).eq("id2", 3).assertOneMatch();
    }

    @Path("e2")
    public static class E2Resource {

        @Context
        private Configuration config;

        @DELETE
        public SimpleResponse deleteE2_Batch(Collection<EntityDelete<E2>> deleted, @Context UriInfo uriInfo) {
            return Ag.service(config).delete(E2.class, deleted);
        }
    }

    @Path("e17")
    public static class E17Resource {

        @Context
        private Configuration config;

        @DELETE
        public SimpleResponse delete_Batch(Collection<EntityDelete<E17>> deleted) {
            return Ag.service(config).delete(E17.class, deleted);
        }
    }

}
