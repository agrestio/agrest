package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E23;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class GET_ExposedIdIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E23.class};
    }

    @Test
    public void testById() {

        e23().insertColumns("id", "name")
                .values(1, "abc")
                .values(2, "xyz").exec();

        Response r = target("/e23").path("1").request().get();
        onSuccess(r).bodyEquals(1, "{\"id\":1,\"exposedId\":1,\"name\":\"abc\"}");
    }

    @Path("e23")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("{id}")
        public DataResponse<E23> getById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.select(E23.class, config).byId(id).uri(uriInfo).getOne();
        }
    }
}
