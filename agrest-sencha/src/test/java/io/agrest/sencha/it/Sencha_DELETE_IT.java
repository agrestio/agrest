package io.agrest.sencha.it;

import io.agrest.Ag;
import io.agrest.EntityDelete;
import io.agrest.SimpleResponse;
import io.agrest.it.fixture.cayenne.E17;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.sencha.it.fixture.SenchaBQJerseyTestOnDerby;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

import static org.junit.Assert.*;

public class Sencha_DELETE_IT extends SenchaBQJerseyTestOnDerby {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(E2Resource.class, E17Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E17.class};
    }

    @Test
    public void test_BatchDelete() {

        e2().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        Response response = target("/e2").request()
                .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                .method("DELETE", Entity.json(" [{\"id\":1},{\"id\":2}]"), Response.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        e2().matcher().assertOneMatch();
        e2().matcher().eq("id", 3).assertOneMatch();
    }

    @Test
    public void test_BatchDelete_CompoundId() {

        e17().insertColumns("id1", "id2", "name")
                .values(1, 1, "aaa")
                .values(2, 2, "bbb")
                .values(3, 3, "ccc").exec();

        Response response = target("/e17").request()
                .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                .method("DELETE", Entity.json("[{\"id1\":1,\"id2\":1},{\"id1\":2,\"id2\":2}]"), Response.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        e17().matcher().assertOneMatch();
        e17().matcher().eq("id1", 3).eq("id2", 3).assertOneMatch();
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
