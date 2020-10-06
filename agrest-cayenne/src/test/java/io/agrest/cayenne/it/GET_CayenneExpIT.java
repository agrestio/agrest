package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class GET_CayenneExpIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class};
    }

    @Test
    public void testMap() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        Response r = target("/e2").queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"name = 'yyy'\"}"))
                .request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testMap_Params() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        Response r = target("/e2").queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"name = $n\",\"params\":{\"n\":\"xxx\"}}")).request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":1}");
    }

    @Test
    public void testBare() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        Response r = target("/e2")
                .queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("name = 'yyy'"))
                .request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testList() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        Response r = target("/e2")
                .queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("[\"name = 'yyy'\"]"))
                .request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testList_Params() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        Response r = target("/e2")
                .queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("[\"name = $b\", \"xxx\"]"))
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":1}");
    }

    @Test
    public void testIn_Array() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzzz").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(6, "yyy", 3)
                .values(8, "yyy", 1)
                .values(9, "zzz", 2).exec();

        Response r = target("/e3").queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"e2 in $ids\",\"params\":{\"ids\": [3, 4]}}")).request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":6}");
    }

    @Test
    public void testNotIn_Array() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 2).exec();

        Response r = target("/e3")
                .queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"e2 not in $ids\",\"params\":{\"ids\": [1, 2]}}"))
                .request()
                .get();

        onSuccess(r).bodyEquals(0, "");
    }

    @Test
    public void testOuter() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "A", 1)
                .values(9, "B", null).exec();

        Response r = target("/e3")
                .queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"e2+.name = null\"}"))
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":9}");
    }

    @Test
    public void testOuter_Relationship() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "A", 1)
                .values(9, "B", null).exec();

        Response r = target("/e3")
                .queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"e2+ = null\"}"))
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":9}");
    }

    @Test
    public void testOuter_To_Many_Relationship() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "A", 1)
                .values(9, "B", null).exec();

        Response r = target("/e2")
                .queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"e3s+ = null\"}"))
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testIn_TwoObjects() {

        e3().insertColumns("id_", "name")
                .values(8, "yyy")
                .values(9, "zzz").exec();

        Response r = target("/e3")
                .queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"name in ($n1, $n2)\",\"params\":{\"n1\":\"zzz\",\"n2\":\"xxx\"}}"))
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":9}");
    }

    @Test
    public void testIn_TwoRelatedObjects() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 2).exec();

        Response r = target("/e3")
                .queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"e2.name in ($n1, $n2)\",\"params\":{\"n1\":\"zzz\",\"n2\":\"xxx\"}}"))
                .request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":8}");
    }

    @Test
    public void testNotIn_ById() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 2).exec();

        Response r = target("/e3").queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"e2 not in ($id)\",\"params\":{\"id\":1}}"))
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":9}");
    }

    @Test
    public void testNotIn_By2Ids() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 2).exec();

        Response r = target("/e3")
                .queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"e2 not in ($id1, $id2)\",\"params\":{\"id1\":1,\"id2\":3}}")).request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":9}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E2.class).uri(uriInfo).get();
        }

        @GET
        @Path("e3")
        public DataResponse<E3> getE3(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E3.class).uri(uriInfo).get();
        }
    }
}
