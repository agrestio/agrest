package io.agrest.it;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.ResourceEntity;
import io.agrest.SelectStage;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EncoderFilter;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.runtime.AgBuilder;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GET_EncoderFilters_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Override
    protected AgBuilder doConfigure() {
        return super.doConfigure().encoderFilter(new E4OddFilter());
    }

    @Test
    public void testFilteredTotal() {

        newContext()
                .performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e4 (id) values (1), (2)"));

        Response response = target("/e4")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .request()
                .get();

        onSuccess(response).bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testFilteredPagination1() {

        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e4 (id) "
                        + "values (1), (2), (3), (4), (5), (6), (7), (8), (9), (10)"));

        Response response1 = target("/e4").queryParam("include", "id").queryParam("sort", "id")
                .queryParam("start", "0").queryParam("limit", "2").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":2},{\"id\":4}],\"total\":5}",
                response1.readEntity(String.class));
    }

    @Test
    public void testFilteredPagination2() {

        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e4 (id) "
                        + "values (1), (2), (3), (4), (5), (6), (7), (8), (9), (10)"));

        Response response1 = target("/e4").queryParam("include", "id").queryParam("sort", "id")
                .queryParam("start", "2").queryParam("limit", "3").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":6},{\"id\":8},{\"id\":10}],\"total\":5}",
                response1.readEntity(String.class));
    }

    @Test
    public void testFilteredPagination3() {

        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e4 (id) "
                        + "values (1), (2), (3), (4), (5), (6), (7), (8), (9), (10)"));

        Response response1 = target("/e4").queryParam("include", "id").queryParam("sort", "id")
                .queryParam("start", "2").queryParam("limit", "10").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":6},{\"id\":8},{\"id\":10}],\"total\":5}",
                response1.readEntity(String.class));
    }

    @Test
    public void testFilteredPagination4_CustomStage() {

        Resource.RESOURCE_ENTITY_IS_FILTERED = false;
        Resource.QUERY_PAGE_SIZE = 0;

        target("/e4/pagination_stage")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .queryParam("start", "2")
                .queryParam("limit", "10")
                .request().get();

        assertTrue(Resource.RESOURCE_ENTITY_IS_FILTERED);
        assertEquals(0, Resource.QUERY_PAGE_SIZE);
    }

    private final class E4OddFilter implements EncoderFilter {
        @Override
        public boolean matches(ResourceEntity<?, ?> entity) {
            return entity.getAgEntity().getName().equals("E4");
        }

        @Override
        public boolean encode(String propertyName, Object object, JsonGenerator out, Encoder delegate)
                throws IOException {

            E4 e4 = (E4) object;

            // keep even, remove odd
            if (Cayenne.intPKForObject(e4) % 2 == 0) {
                return delegate.encode(propertyName, object, out);
            }

            return false;
        }

        @Override
        public boolean willEncode(String propertyName, Object object, Encoder delegate) {
            E4 e4 = (E4) object;

            // keep even, remove odd
            if (Cayenne.intPKForObject(e4) % 2 == 0) {
                return delegate.willEncode(propertyName, object);
            }

            return false;
        }
    }

    @Path("")
    public static class Resource {

        static boolean RESOURCE_ENTITY_IS_FILTERED;
        static int QUERY_PAGE_SIZE;

        @Context
        private Configuration config;


        @GET
        @Path("e4")
        public DataResponse<E4> get(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E4.class).uri(uriInfo).get();
        }

        @GET
        @Path("e4/pagination_stage")
        public DataResponse<E4> get_WithPaginationStage(@Context UriInfo uriInfo) {
            return Ag.service(config)
                    .select(E4.class)
                    .uri(uriInfo)
                    .stage(SelectStage.APPLY_SERVER_PARAMS,
                            c -> RESOURCE_ENTITY_IS_FILTERED = c.getEntity().isFiltered())
//                    .stage(SelectStage.ASSEMBLE_QUERY,
//                            c -> QUERY_PAGE_SIZE = c.getSelect().getPageSize())
                    .get();
        }
    }
}
