package io.agrest.it;

import io.agrest.DataResponse;
import io.agrest.LinkRest;
import io.agrest.UpdateStage;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E3;
import org.junit.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PUT_StagesIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    @Deprecated
    public void testPut_ToOne() {
        insert("e3", "id, name", "3, 'z'");
        insert("e3", "id, name", "4, 'a'");


        Resource.BEFORE_UPDATE_CALLED = false;

        Response response = target("/e3/callbackstage")
                .request()
                .put(Entity.json("[{\"id\":3,\"name\":\"x\"}]"));

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"name\":\"x\",\"phoneNumber\":null}");

        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND name = 'x'"));
        assertEquals(0, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 4"));

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
            return LinkRest.idempotentFullSync(E3.class, config)
                    .stage(UpdateStage.APPLY_SERVER_PARAMS, c -> BEFORE_UPDATE_CALLED = true)
                    .uri(uriInfo)
                    .syncAndSelect(requestBody);
        }
    }
}
