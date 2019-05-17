package io.agrest.it;

import io.agrest.Ag;
import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E3;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

public class PUT_EntityUpdateBindingIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E3.class};
    }

    @Test
    public void testSingle() {

        e3().insertColumns("id", "name").values(3, "zzz").exec();

        Response response = target("/e3/updatebinding/3")
                .request()
                .put(Entity.json("{\"id\":3,\"name\":\"yyy\"}"));

        onSuccess(response).bodyEquals("{\"success\":true}");
        e3().matcher().eq("id", 3).eq("name", "yyy").assertOneMatch();
    }

    @Test
    public void testCollection() {

        e3().insertColumns("id", "name")
                .values(3, "zzz")
                .values(4, "xxx")
                .values(5, "mmm").exec();

        Response response = target("/e3/updatebinding")
                .request()
                .put(Entity.json("[{\"id\":3,\"name\":\"yyy\"},{\"id\":5,\"name\":\"nnn\"}]"));

        onSuccess(response).bodyEquals("{\"success\":true}");

        e3().matcher().assertMatches(2);
        e3().matcher().eq("id", 3).eq("name", "yyy").assertOneMatch();
        e3().matcher().eq("id", 5).eq("name", "nnn").assertOneMatch();
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
            return Ag.idempotentFullSync(E3.class, config).uri(uriInfo).sync(entityUpdates);
        }

        @PUT
        @Path("e3/updatebinding/{id}")
        public SimpleResponse updateE3_EntityUpdateSingle(
                @PathParam("id") int id,
                EntityUpdate<E3> update) {

            return Ag.createOrUpdate(E3.class, config).id(id).sync(update);
        }
    }
}
