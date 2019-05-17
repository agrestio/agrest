package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E5;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class GET_IncludeIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E5.class};
    }

    @Test
    public void test_Include_OrderOfInclude() {

        e5().insertColumns("id", "name", "date").values(45, "T", "2013-01-03").exec();
        e2().insertColumns("id", "name").values(8, "yyy").exec();
        e3().insertColumns("id", "name", "e2_id", "e5_id").values(3, "zzz", 8, 45).exec();

        Response r1 = target("/e3")
                .queryParam("include", "id")
                .queryParam("include", "e2")
                .queryParam("include", "e2.id")
                .request()
                .get();

        onSuccess(r1).bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":8}}");

        // change the order of includes
        Response r2 = target("/e3")
                .queryParam("include", "id")
                .queryParam("include", "e2.id")
                .queryParam("include", "e2")
                .request()
                .get();

        onSuccess(r2).bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":8}}");
    }

    @Test
    public void test_Include_Phantom() {
        e5().insertColumns("id", "name", "date").values(45, "T", "2013-01-03").exec();
        e2().insertColumns("id", "name").values(8, "yyy").exec();
        e3().insertColumns("id", "name", "e2_id", "e5_id").values(3, "zzz", 8, 45).exec();

        Response r1 = target("/e2")
                .queryParam("include", "id")
                .queryParam("include", "e3s.e5.id")
                .request()
                .get();

        onSuccess(r1).bodyEquals(1, "{\"id\":8,\"e3s\":[{\"e5\":{\"id\":45}}]}");
    }


    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E2.class).uri(uriInfo).get();
        }

        @GET
        @Path("e3")
        public DataResponse<E3> getE3(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E3.class).uri(uriInfo).get();
        }
    }
}
