package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;

import io.agrest.it.fixture.cayenne.E4;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class GET_SizeConstraintsIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E4.class};
    }

    // TODO: unclear what server-side fetch offset protects? so not testing it here.

    @Test
    public void testNoClientLimit() {

        e4().insertColumns("id")
                .values(1)
                .values(2)
                .values(3).exec();

        Response r = target("/e4/limit")
                .queryParam("sort", "id")
                .queryParam("include", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(3, "{\"id\":1},{\"id\":2}");
    }

    @Test
    public void testClientLimitBelowServerLimit() {

        e4().insertColumns("id")
                .values(1)
                .values(2)
                .values(3).exec();

        Response r = target("/e4/limit")
                .queryParam("sort", "id")
                .queryParam("include", "id")
                .queryParam("limit", "1")
                .request()
                .get();

        onSuccess(r).bodyEquals(3, "{\"id\":1}");
    }

    @Test
    public void testClientLimitExceedsServerLimit() {

        e4().insertColumns("id")
                .values(1)
                .values(2)
                .values(3).exec();

        Response r = target("/e4/limit")
                .queryParam("sort", "id")
                .queryParam("include", "id")
                .queryParam("limit", "5")
                .request()
                .get();

        onSuccess(r).bodyEquals(3, "{\"id\":1},{\"id\":2}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e4/limit")
        public DataResponse<E4> limit(@Context UriInfo uriInfo) {
            return Ag.select(E4.class, config).uri(uriInfo)
                    .fetchLimit(2)
                    .get();
        }
    }
}
