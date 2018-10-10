package io.agrest.it;

import io.agrest.DataResponse;
import io.agrest.Ag;
import io.agrest.SelectStage;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E14;
import io.agrest.it.fixture.cayenne.E15;
import io.agrest.it.fixture.pojo.model.P7;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.Cayenne;
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

public class GET_Props_PersistentWithExtraAnnotatedProps_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testGET_Root() {
        insert("e15", "long_id, name", "1, 'xxx'");
        insert("e14", "e15_id, long_id, name", "1, 8, 'yyy'");

        Response response1 = target("/e14").queryParam("include", "name").queryParam("include", "prettyName").request()
                .get();
        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}],\"total\":1}",
                response1.readEntity(String.class));
    }

    @Test
    public void testGET_PrefetchPojoRel() {
        insert("e15", "long_id, name", "1, 'xxx'");
        insert("e14", "e15_id, long_id, name", "1, 8, 'yyy'");

        Response response1 = target("/e14").queryParam("include", "name").queryParam("include", "p7").request().get();
        assertEquals(Status.OK.getStatusCode(), response1.getStatus());

        assertEquals("{\"data\":[{\"name\":\"yyy\",\"p7\":{\"id\":800,\"string\":\"p7_yyy\"}}],\"total\":1}",
                response1.readEntity(String.class));
    }

    @Test
    public void testGET_Related() {
        insert("e15", "long_id, name", "1, 'xxx'");
        insert("e14", "e15_id, long_id, name", "1, 8, 'yyy'");

        Response response1 = target("/e15").queryParam("include", "e14s.name").queryParam("include", "e14s.prettyName")
                .request().get();
        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals(
                "{\"data\":"
                        + "[{\"id\":1,\"e14s\":[{\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}],\"name\":\"xxx\"}],\"total\":1}",
                response1.readEntity(String.class));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e15")
        public DataResponse<E15> getE15(@Context UriInfo uriInfo) {
            return Ag.select(E15.class, config).uri(uriInfo).get();
        }

        @GET
        @Path("e14")
        public DataResponse<E14> getE14(@Context UriInfo uriInfo) {
            return Ag.select(E14.class, config)
                    .stage(SelectStage.FETCH_DATA, (SelectContext<E14> c) -> afterE14Fetched(c))
                    .uri(uriInfo).get();
        }

        void afterE14Fetched(SelectContext<E14> context) {
            for (E14 e14 : context.getObjects()) {
                P7 p7 = new P7();
                p7.setId(Cayenne.intPKForObject(e14) * 100);
                p7.setString("p7_" + e14.getName());
                e14.setP7(p7);
            }
        }
    }
}
