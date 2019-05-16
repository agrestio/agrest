package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.UpdateStage;
import io.agrest.it.fixture.BQJerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E3;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.*;

public class PUT_StagesIT extends BQJerseyTestOnDerby {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E3.class};
    }

    @Test
    public void testPut_ToOne() {

        e3().insertColumns("id", "name")
                .values(3, "z")
                .values(4, "a").exec();

        Resource.BEFORE_UPDATE_CALLED = false;

        Response response = target("/e3/callbackstage")
                .request()
                .put(Entity.json("[{\"id\":3,\"name\":\"x\"}]"));

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"name\":\"x\",\"phoneNumber\":null}");

        e3().matcher().eq("id", 3).eq("name", "x").assertOneMatch();
        e3().matcher().eq("id", 4).assertNoMatches();

        assertTrue(Resource.BEFORE_UPDATE_CALLED);
    }


    @Path("")
    public static class Resource {

        public static boolean BEFORE_UPDATE_CALLED;

        @Context
        private Configuration config;

        @PUT
        @Path("e3/callbackstage")
        public DataResponse<E3> syncWithCallbackStage(@Context UriInfo uriInfo, String requestBody) {
            return Ag.idempotentFullSync(E3.class, config)
                    .stage(UpdateStage.APPLY_SERVER_PARAMS, c -> BEFORE_UPDATE_CALLED = true)
                    .uri(uriInfo)
                    .syncAndSelect(requestBody);
        }
    }
}
