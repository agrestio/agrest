package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E23;
import io.agrest.it.fixture.cayenne.E26;
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
        return new Class[]{E23.class, E26.class};
    }

    @Test
    public void testById() {

        e23().insertColumns("id", "name")
                .values(1, "abc")
                .values(2, "xyz").exec();

        Response r = target("/e23").path("1").request().get();
        onSuccess(r).bodyEquals(1, "{\"id\":1,\"exposedId\":1,\"name\":\"abc\"}");
    }

    @Test
    public void testIncludeFrom() {

        e23().insertColumns("id", "name").values(1, "abc").exec();
        e26().insertColumns("id", "e23_id").values(41, 1).exec();

        Response r = target("/e23")
                .queryParam("include", "id", "e26s.id")
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":1,\"e26s\":[{\"id\":41}]}").ranQueries(2);
    }

    @Test
    public void testIncludeTo() {

        e23().insertColumns("id", "name").values(1, "abc").exec();
        e26().insertColumns("id", "e23_id").values(41, 1).exec();

        Response r = target("/e26")
                .queryParam("include", "id", "e23.id")
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":41,\"e23\":{\"id\":1}}").ranQueries(2);
    }


    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e26")
        public DataResponse<E26> getAllE26s(@Context UriInfo uriInfo) {
            return Ag.select(E26.class, config).uri(uriInfo).getOne();
        }

        @GET
        @Path("e23")
        public DataResponse<E23> getAllE23s(@Context UriInfo uriInfo) {
            return Ag.select(E23.class, config).uri(uriInfo).getOne();
        }

        @GET
        @Path("e23/{id}")
        public DataResponse<E23> getById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.select(E23.class, config).byId(id).uri(uriInfo).getOne();
        }
    }
}
