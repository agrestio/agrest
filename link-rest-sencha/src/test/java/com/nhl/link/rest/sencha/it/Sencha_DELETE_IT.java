package com.nhl.link.rest.sencha.it;

import com.nhl.link.rest.EntityDelete;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E17;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class Sencha_DELETE_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E2Resource.class);
        context.register(E17Resource.class);
    }

    @Test
    public void test_BatchDelete()  {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e2", "id, name", "3, 'zzz'");

        Response response1 = target("/e2").request()
                .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                .method("DELETE", Entity.entity(" [{\"id\":1},{\"id\":2}]",
                        MediaType.APPLICATION_JSON), Response.class);

        assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e2"));
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e2 WHERE id = 3"));
    }

    @Test
    public void test_BatchDelete_CompoundId()  {

        insert("e17", "id1, id2, name", "1, 1, 'aaa'");
        insert("e17", "id1, id2, name", "2, 2, 'bbb'");
        insert("e17", "id1, id2, name", "3, 3, 'ccc'");

        Response response1 = target("/e17").request()
                .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                .method("DELETE", Entity.entity("[{\"id1\":1,\"id2\":1},{\"id1\":2,\"id2\":2}]",
                        MediaType.APPLICATION_JSON), Response.class);

        assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e17"));
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e17 WHERE id1 = 3 AND id2 = 3"));
    }

    @Path("e2")
    public static class E2Resource {

        @Context
        private Configuration config;

        @DELETE
        public SimpleResponse deleteE2_Batch(Collection<EntityDelete<E2>> deleted, @Context UriInfo uriInfo) {
            return LinkRest.service(config).delete(E2.class, deleted);
        }
    }

    @Path("e17")
    public static class E17Resource {

        @Context
        private Configuration config;

        @DELETE
        public SimpleResponse delete_Batch(Collection<EntityDelete<E17>> deleted) {
            return LinkRest.service(config).delete(E17.class, deleted);
        }
    }

}
