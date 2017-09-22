package com.nhl.link.rest.it;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.SelectStage;
import com.nhl.link.rest.annotation.listener.QueryAssembled;
import com.nhl.link.rest.annotation.listener.SelectServerParamsApplied;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.LinkRestBuilder;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.ObjectSelect;
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
    protected LinkRestBuilder doConfigure() {
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

    @Deprecated
    @Test
    public void testFilteredPagination4_Listeners() {

        CayennePaginationListener.RESOURCE_ENTITY_IS_FILTERED = false;
        CayennePaginationListener.QUERY_PAGE_SIZE = 1;

        target("/e4/pagination_listener")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .queryParam("start", "2")
                .queryParam("limit", "10")
                .request()
                .get();

        assertTrue(CayennePaginationListener.RESOURCE_ENTITY_IS_FILTERED);
        assertEquals(0, CayennePaginationListener.QUERY_PAGE_SIZE);
    }

    @Test
    public void testFilteredPagination4_CustomStage() {

        Resource.RESOURCE_ENTITY_IS_FILTERED = false;
        Resource.QUERY_PAGE_SIZE = 1;

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
        public boolean matches(ResourceEntity<?> entity) {
            return entity.getLrEntity().getName().equals("E4");
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
            return LinkRest.service(config).select(E4.class).uri(uriInfo).get();
        }

        /**
         * @deprecated since 2.7 as listeners are deprecated.
         */
        @GET
        @Path("e4/pagination_listener")
        @Deprecated
        public DataResponse<E4> get_WithPaginationListener(@Context UriInfo uriInfo) {
            return LinkRest.service(config).select(E4.class).uri(uriInfo).listener(new CayennePaginationListener())
                    .get();
        }

        @GET
        @Path("e4/pagination_stage")
        public DataResponse<E4> get_WithPaginationStage(@Context UriInfo uriInfo) {
            return LinkRest.service(config)
                    .select(E4.class)
                    .uri(uriInfo)
                    .stage(SelectStage.APPLY_SERVER_PARAMS,
                            c -> RESOURCE_ENTITY_IS_FILTERED = c.getEntity().isFiltered())
                    .stage(SelectStage.ASSEMBLE_QUERY,
                            c -> QUERY_PAGE_SIZE = ((ObjectSelect<?>)c.getSelect()).getPageSize())
                    .get();
        }
    }

    /**
     * @deprecated since 2.7 as listeners are deprecated.
     */
    @Deprecated
    public static class CayennePaginationListener {

        public static boolean RESOURCE_ENTITY_IS_FILTERED;
        public static int QUERY_PAGE_SIZE;

        @Deprecated
        @SelectServerParamsApplied
        public <T> ProcessingStage<SelectContext<T>, T> selectServerParamsApplied(
                SelectContext<T> context,
                ProcessingStage<SelectContext<T>, T> next) {

            RESOURCE_ENTITY_IS_FILTERED = context.getEntity().isFiltered();
            return next;
        }

        @Deprecated
        @QueryAssembled
        public <T> ProcessingStage<SelectContext<T>, T> queryAssembled(
                SelectContext<T> context,
                ProcessingStage<SelectContext<T>, T> next) {

            QUERY_PAGE_SIZE = ((ObjectSelect<?>)context.getSelect()).getPageSize();
            return next;
        }
    }
}
