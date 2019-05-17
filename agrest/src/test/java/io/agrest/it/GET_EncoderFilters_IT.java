package io.agrest.it;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.ResourceEntity;
import io.agrest.SelectStage;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EncoderFilter;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.runtime.AgBuilder;
import org.apache.cayenne.Cayenne;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.function.UnaryOperator;

import static org.junit.Assert.*;

public class GET_EncoderFilters_IT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        UnaryOperator<AgBuilder> customizer = ab -> ab.encoderFilter(new E4OddFilter());
        startTestRuntime(customizer, Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E4.class};
    }

    @Test
    public void testFilteredTotal() {

        e4().insertColumns("id").values(1).values(2).exec();

        Response r = target("/e4")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testFilteredPagination1() {

        e4().insertColumns("id")
                .values(1)
                .values(2)
                .values(3)
                .values(4)
                .values(5)
                .values(6)
                .values(7)
                .values(8)
                .values(9)
                .values(10).exec();

        Response r = target("/e4")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .queryParam("start", "0")
                .queryParam("limit", "2")
                .request()
                .get();

        onSuccess(r).bodyEquals(5, "{\"id\":2},{\"id\":4}");
    }

    @Test
    public void testFilteredPagination2() {

        e4().insertColumns("id")
                .values(1)
                .values(2)
                .values(3)
                .values(4)
                .values(5)
                .values(6)
                .values(7)
                .values(8)
                .values(9)
                .values(10).exec();

        Response r = target("/e4")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .queryParam("start", "2")
                .queryParam("limit", "3")
                .request().get();

        onSuccess(r).bodyEquals(5, "{\"id\":6},{\"id\":8},{\"id\":10}");
    }

    @Test
    public void testFilteredPagination3() {

        e4().insertColumns("id")
                .values(1)
                .values(2)
                .values(3)
                .values(4)
                .values(5)
                .values(6)
                .values(7)
                .values(8)
                .values(9)
                .values(10).exec();

        Response r = target("/e4")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .queryParam("start", "2")
                .queryParam("limit", "10")
                .request().get();

        onSuccess(r).bodyEquals(5, "{\"id\":6},{\"id\":8},{\"id\":10}");
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

    private static class E4OddFilter implements EncoderFilter {
        @Override
        public boolean matches(ResourceEntity<?> entity) {
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
                    .stage(SelectStage.ASSEMBLE_QUERY,
                            c -> QUERY_PAGE_SIZE = c.getEntity().getSelect().getPageSize())
                    .get();
        }
    }
}
