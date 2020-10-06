package io.agrest.sencha;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E14;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.CayenneAgTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.sencha.ops.unit.SenchaBodyAssertions;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class Sencha_POST_IT extends DbTest {

    @BQTestTool
    static final CayenneAgTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E14.class)
            .build();

    @Test
    public void testPost_ToOne() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.target("/e3")
                .post("{\"e2_id\":8,\"name\":\"MM\"}")
                .wasCreated()
                .replaceId("<ID>")
                .bodyTransformer(SenchaBodyAssertions::checkAndNormalizeBody)
                .bodyEquals(1, "{\"id\":<ID>,\"name\":\"MM\",\"phoneNumber\":null}");
    }

    @Test
    public void testPost_ToOne_BadFK() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.target("/e3")
                .post("{\"e2_id\":15,\"name\":\"MM\"}")
                .wasNotFound();

        tester.e3().matcher().assertNoMatches();
    }

    @Test
    public void testPOST_Bulk_LongId() {

        tester.target("/e14/")
                .post("[{\"id\":\"ext-record-6881\",\"name\":\"yyy\"},{\"id\":\"ext-record-6882\",\"name\":\"zzz\"}]")
                .wasCreated()
                .totalEquals(2);

        tester.e14().matcher().assertMatches(2);
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e3")
        public DataResponse<E3> create(@Context UriInfo uriInfo, String requestBody) {
            return Ag.create(E3.class, config).uri(uriInfo).syncAndSelect(requestBody);
        }

        @POST
        @Path("e14")
        public DataResponse<E14> post(String data) {
            return Ag.create(E14.class, config).syncAndSelect(data);
        }
    }
}
