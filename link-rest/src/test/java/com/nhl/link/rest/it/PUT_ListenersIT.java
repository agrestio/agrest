package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.listener.UpdateCallbackListener;
import org.junit.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static com.nhl.link.rest.unit.matcher.LRMatchers.okAndHasData;
import static org.junit.Assert.*;

/**
 * @deprecated since 2.7 as listeners API is deprecated.
 */
@Deprecated
public class PUT_ListenersIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testPut_ToOne() {
        insert("e3", "id, name", "3, 'z'");
        insert("e3", "id, name", "4, 'a'");


        UpdateCallbackListener.BEFORE_UPDATE_CALLED = false;

        Response response = target("/e3/callbacklistener").request()
                .put(Entity.json("[{\"id\":3,\"name\":\"x\"}]"));

        assertThat(response, okAndHasData(1, "[{\"id\":3,\"name\":\"x\",\"phoneNumber\":null}]"));
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND name = 'x'"));
        assertEquals(0, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 4"));

        assertTrue(UpdateCallbackListener.BEFORE_UPDATE_CALLED);
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        /**
         * @deprecated since 2.7
         */
        @PUT
        @Path("e3/callbacklistener")
        public DataResponse<E3> syncWithCallbackListeners(@Context UriInfo uriInfo, String requestBody) {
            return LinkRest.idempotentFullSync(E3.class, config).listener(new UpdateCallbackListener()).uri(uriInfo)
                    .syncAndSelect(requestBody);
        }
    }
}
