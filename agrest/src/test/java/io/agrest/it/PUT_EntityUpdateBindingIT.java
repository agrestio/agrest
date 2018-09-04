package io.agrest.it;

import io.agrest.AgREST;
import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E3;
import org.junit.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class PUT_EntityUpdateBindingIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testPut_Single() {

        insert("e3", "id, name", "3, 'zzz'");

        Response response = target("/e3/updatebinding/3")
                .request()
                .put(Entity.json("{\"id\":3,\"name\":\"yyy\"}"));

        onSuccess(response).bodyEquals("{\"success\":true}");
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND name = 'yyy'"));
    }

    @Test
    public void testPut_Collection() {
        insert("e3", "id, name", "3, 'zzz'");
        insert("e3", "id, name", "4, 'xxx'");
        insert("e3", "id, name", "5, 'mmm'");

        Response response = target("/e3/updatebinding").request()
                .put(Entity.json("[{\"id\":3,\"name\":\"yyy\"},{\"id\":5,\"name\":\"nnn\"}]"));

        onSuccess(response).bodyEquals("{\"success\":true}");

        assertEquals(2, intForQuery("SELECT COUNT(1) FROM utest.e3"));
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND name = 'yyy'"));
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 5 AND name = 'nnn'"));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e3/updatebinding")
        public SimpleResponse sync_EntityUpdateCollection(
                @Context UriInfo uriInfo,
                Collection<EntityUpdate<E3>> entityUpdates) {
            return AgREST.idempotentFullSync(E3.class, config).uri(uriInfo).sync(entityUpdates);
        }

        @PUT
        @Path("e3/updatebinding/{id}")
        public SimpleResponse updateE3_EntityUpdateSingle(
                @PathParam("id") int id,
                EntityUpdate<E3> update) {

            return AgREST.createOrUpdate(E3.class, config).id(id).sync(update);
        }
    }
}
