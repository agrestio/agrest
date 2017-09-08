package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.listener.FetchCallbackListener;
import com.nhl.link.rest.it.fixture.listener.FetchPassThroughListener;
import com.nhl.link.rest.it.fixture.listener.FetchTakeOverListener;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @deprecated since 2.7 as listeners are deprecated.
 */
public class GET_ListenersIT extends JerseyTestOnDerby {

    @Before
    public void loadData() {
        newContext()
                .performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));
    }

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testCallbackListener() {

        FetchCallbackListener.BEFORE_FETCH_CALLED = false;

        Response response1 = target("/e3/callbacklistener").queryParam("include", "id").queryParam("sort", "id")
                .request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertTrue(FetchCallbackListener.BEFORE_FETCH_CALLED);

        assertEquals("{\"data\":[{\"id\":8},{\"id\":9}],\"total\":2}",
                response1.readEntity(String.class));
    }

    @Test
    public void testPassThroughLisetner() {

        FetchPassThroughListener.BEFORE_FETCH_CALLED = false;

        Response response1 = target("/e3/passthroughlistener").queryParam("include", "id").queryParam("sort", "id")
                .request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertTrue(FetchPassThroughListener.BEFORE_FETCH_CALLED);

        assertEquals("{\"data\":[{\"id\":8},{\"id\":9}],\"total\":2}",
                response1.readEntity(String.class));
    }

    @Test
    public void testTakeOverListener() {

        FetchTakeOverListener.BEFORE_FETCH_CALLED = false;

        Response response1 = target("/e3/takeoverlistener").queryParam("include", "name").queryParam("sort", "id")
                .request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertTrue(FetchTakeOverListener.BEFORE_FETCH_CALLED);

        assertEquals("{\"data\":[{\"name\":\"__X__\"},{\"name\":\"__Y__\"}],\"total\":2}",
                response1.readEntity(String.class));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;


        /**
         * @deprecated since 2.7 as listener API is deprecated.
         */
        @GET
        @Path("e3/callbacklistener")
        public DataResponse<E3> getWithCallbackListeners(@Context UriInfo uriInfo) {
            return LinkRest.service(config).select(E3.class).listener(new FetchCallbackListener()).uri(uriInfo).get();
        }

        /**
         * @deprecated since 2.7 as listener API is deprecated.
         */
        @GET
        @Path("e3/passthroughlistener")
        public DataResponse<E3> getWithPassThroughListeners(@Context UriInfo uriInfo) {
            return LinkRest.service(config).select(E3.class).listener(new FetchPassThroughListener()).uri(uriInfo).get();
        }

        /**
         * @deprecated since 2.7 as listener API is deprecated.
         */
        @GET
        @Path("e3/takeoverlistener")
        public DataResponse<E3> getWithTakeOverListeners(@Context UriInfo uriInfo) {
            return LinkRest.service(config).select(E3.class).listener(new FetchTakeOverListener()).uri(uriInfo).get();
        }
    }

}
