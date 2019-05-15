package io.agrest.it;

import io.agrest.Ag;
import io.agrest.SimpleResponse;
import io.agrest.it.fixture.BQJerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E17;
import io.agrest.it.fixture.cayenne.E24;
import io.agrest.it.fixture.cayenne.E4;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class DELETE_IT extends BQJerseyTestOnDerby {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E4.class, E17.class, E24.class};
    }

    @Test
    public void testDelete() {

        e4().insertColumns("id", "c_varchar")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        Response r = target("/e4/8").request().delete();
        onSuccess(r).bodyEquals("{\"success\":true}");

        e4().matcher().assertOneMatch();
    }

    @Test
    public void testDelete_CompoundId() {

        e17().insertColumns("id1", "id2", "name").values(1, 1, "aaa").values(2, 2, "bbb").exec();

        Response r = target("/e17").queryParam("id1", 1).queryParam("id2", 1).request().delete();
        onSuccess(r).bodyEquals("{\"success\":true}");

        e17().matcher().assertOneMatch();
        e17().matcher().eq("id2", 2).eq("id2", 2).eq("name", "bbb").assertOneMatch();
    }

    @Test
    public void testDelete_BadID() {

        e4().insertColumns("id", "c_varchar").values(1, "xxx").exec();

        Response r = target("/e4/7").request().delete();
        onResponse(r).statusEquals(Status.NOT_FOUND).bodyEquals("{\"success\":false,\"message\":\"No object for ID '7' and entity 'E4'\"}");

        e4().matcher().assertMatches(1);
    }

    @Test
    public void testDelete_Twice() {

        e4().insertColumns("id", "c_varchar")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        Response r1 = target("/e4/8").request().delete();
        onSuccess(r1).bodyEquals("{\"success\":true}");

        Response r2 = target("/e4/8").request().delete();
        onResponse(r2).statusEquals(Status.NOT_FOUND).bodyEquals("{\"success\":false,\"message\":\"No object for ID '8' and entity 'E4'\"}");
    }

    @Test
    public void test_Delete_UpperCasePK() {

        e24().insertColumns("TYPE", "name").values(1, "xyz").exec();

        Response r = target("/e24/1").request().delete();
        onSuccess(r).bodyEquals("{\"success\":true}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("e4/{id}")
        public SimpleResponse deleteById(@PathParam("id") int id) {
            return Ag.service(config).delete(E4.class, id);
        }

        @DELETE
        @Path("e17")
        public SimpleResponse deleteByMultiId(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2) {

            Map<String, Object> ids = new HashMap<>();
            ids.put(E17.ID1_PK_COLUMN, id1);
            ids.put(E17.ID2_PK_COLUMN, id2);

            return Ag.service(config).delete(E17.class, ids);
        }

        @DELETE
        @Path("e24/{id}")
        public SimpleResponse deleteE24ById(@PathParam("id") int id) {
            return Ag.delete(E24.class, config).id(id).delete();
        }
    }
}
