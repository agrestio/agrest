package io.agrest.runtime.provider;

import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.protocol.Dir;
import io.agrest.protocol.Exclude;
import io.agrest.protocol.Include;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static org.junit.Assert.*;

public class IncludeExcludeProviderIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }


    @Test
    public void test_Select_RelationshipStartLimit() throws UnsupportedEncodingException {

        Response response = target("/e2_RelationshipStartLimit")
                .queryParam("include", "id")
                .queryParam("include", URLEncoder.encode("{\"path\":\"" + E2.E3S.getName() + "\",\"start\":1,\"limit\":1}", "UTF-8"))
                .queryParam("exclude", E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .request().get();

        assertNotNull(response);
    }

    @Test
    public void test_Select_Prefetching_StartLimit() {

        Response response = target("/e3_Prefetching_StartLimit")
                .queryParam("include", "id", "e2.id")
                .queryParam("start", "1")
                .queryParam("limit", "2")
                .request()
                .get();

        assertNotNull(response);
    }

    @Test
    public void test_MapBy_WithCayenneExp() {

        Response response = target("/e2_MapBy_WithCayenneExp")
                .queryParam("include", "id")
                .queryParam("include",
                        urlEnc("{\"path\":\"e3s\",\"mapBy\":\"name\", \"cayenneExp\":{\"exp\":\"name != NULL\"}}"))
                .request().get();

        assertNotNull(response);
    }

    @Test
    public void test_SortPath_Dir() {

        Response response = target("/e2_SortPath_Dir")
                .queryParam("include", "id")
                .queryParam(
                        "include",
                        urlEnc("{\"path\":\"e3s\",\"sort\":[{\"property\":\"e5.name\", \"direction\":\"DESC\"},{\"property\":\"name\", \"direction\":\"DESC\"}]}"))
                .request().get();

        assertNotNull(response);
    }


    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2_RelationshipStartLimit")
        public DataResponse<E2> getE2_RelationshipStartLimit(@QueryParam("include") List<Include> includes,
                                                             @QueryParam("exclude") List<Exclude> excludes) {

            assertNotNull(includes);
            assertEquals(2, includes.size());
            assertEquals("id", includes.get(0).getValue());
            assertEquals(E2.E3S.getName(), includes.get(1).getPath());

            assertNotNull(includes.get(1).getStart());
            assertEquals(Integer.valueOf(1), includes.get(1).getStart());
            assertNotNull(includes.get(1).getLimit());
            assertEquals(Integer.valueOf(1), includes.get(1).getLimit());

            assertNotNull(excludes);
            assertEquals(1, excludes.size());
            assertEquals(E2.E3S.dot(E3.PHONE_NUMBER).getName(), excludes.get(0).getPath());

            return DataResponse.forType(E2.class);
        }

        @GET
        @Path("e2_MapBy_WithCayenneExp")
        public DataResponse<E2> getE2_MapBy_WithCayenneExp(@QueryParam("include") List<Include> includes) {

            assertNotNull(includes);
            assertEquals(2, includes.size());
            assertEquals("id", includes.get(0).getValue());
            assertEquals(E2.E3S.getName(), includes.get(1).getPath());

            assertNotNull(includes.get(1).getMapBy());
            assertEquals("name", includes.get(1).getMapBy());

            assertNotNull(includes.get(1).getCayenneExp());
            assertEquals("name != NULL", includes.get(1).getCayenneExp().getExp());
            assertTrue(includes.get(1).getCayenneExp().getParams().isEmpty());
            assertTrue(includes.get(1).getCayenneExp().getInPositionParams().isEmpty());

            return DataResponse.forType(E2.class);
        }

        @GET
        @Path("e2_SortPath_Dir")
        public DataResponse<E2> getE2_SortPath_Dir(@QueryParam("include") List<Include> includes) {

            assertNotNull(includes);
            assertEquals(2, includes.size());
            assertEquals("id", includes.get(0).getValue());
            assertEquals(E2.E3S.getName(), includes.get(1).getPath());

            assertNotNull(includes.get(1).getSort());
            assertEquals(2, includes.get(1).getSort().getSorts().size());
            assertEquals("e5.name", includes.get(1).getSort().getSorts().get(0).getProperty());
            Assert.assertEquals(Dir.DESC, includes.get(1).getSort().getSorts().get(0).getDirection());
            assertEquals("name", includes.get(1).getSort().getSorts().get(1).getProperty());
            assertEquals(Dir.DESC, includes.get(1).getSort().getSorts().get(1).getDirection());

            return DataResponse.forType(E2.class);
        }

        @GET
        @Path("e3_Prefetching_StartLimit")
        public DataResponse<E3> getE3_Prefetching_StartLimit(
                @QueryParam("include") List<Include> includes,
                @QueryParam("start") Integer start,
                @QueryParam("limit") Integer limit) {

            assertNotNull(includes);
            assertEquals(2, includes.size());
            assertEquals("id", includes.get(0).getValue());
            assertEquals("e2.id", includes.get(1).getValue());
            assertNotNull(start);
            assertEquals(Integer.valueOf(1), start);
            assertNotNull(limit);
            assertEquals(Integer.valueOf(2), limit);

            return DataResponse.forType(E3.class);

        }
    }

}
