package io.agrest.cayenne;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.SelectStage;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EntityEncoderFilter;
import io.agrest.cayenne.cayenne.main.E4;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.Cayenne;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GET_ReadFilter_EntityEncoderFilterIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E4.class)
            .agCustomizer(ab -> ab.entityEncoderFilter(evenFilter()))
            .build();

    static EntityEncoderFilter evenFilter() {
        return EntityEncoderFilter
                .forEntity(E4.class)
                .objectCondition(GET_ReadFilter_EntityEncoderFilterIT::willEncodeEven)
                .encoder(GET_ReadFilter_EntityEncoderFilterIT::encodeEven)
                .build();
    }

    static boolean willEncodeEven(String p, E4 e4, Encoder d) {
        return Cayenne.intPKForObject(e4) % 2 == 0 && d.willEncode(p, e4);
    }

    static boolean encodeEven(String p, E4 e4, JsonGenerator out, Encoder d) throws IOException {
        return Cayenne.intPKForObject(e4) % 2 == 0 && d.encode(p, e4, out);
    }

    @Test
    public void testFilteredTotal() {

        tester.e4().insertColumns("id").values(1).values(2).exec();

        tester.target("/e4")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testFilteredPagination1() {

        tester.e4().insertColumns("id")
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

        tester.target("/e4")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .queryParam("start", "0")
                .queryParam("limit", "2")
                .get()
                .wasOk().bodyEquals(5, "{\"id\":2},{\"id\":4}");
    }

    @Test
    public void testFilteredPagination2() {

        tester.e4().insertColumns("id")
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

        tester.target("/e4")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .queryParam("start", "2")
                .queryParam("limit", "3")
                .get()
                .wasOk().bodyEquals(5, "{\"id\":6},{\"id\":8},{\"id\":10}");
    }

    @Test
    public void testFilteredPagination3() {

        tester.e4().insertColumns("id")
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

        tester.target("/e4")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .queryParam("start", "2")
                .queryParam("limit", "10")
                .get()
                .wasOk().bodyEquals(5, "{\"id\":6},{\"id\":8},{\"id\":10}");
    }

    @Test
    public void testFilteredPagination4_CustomStage() {

        Resource.RESOURCE_ENTITY_IS_FILTERED = false;
        Resource.QUERY_PAGE_SIZE = 0;

        tester.target("/e4/pagination_stage")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .queryParam("start", "2")
                .queryParam("limit", "10")
                .get();

        assertTrue(Resource.RESOURCE_ENTITY_IS_FILTERED);
        assertEquals(0, Resource.QUERY_PAGE_SIZE);
    }

    @Test
    public void testRequestEncoder() {

        tester.e4().insertColumns("id", "c_varchar").values(1, "a").values(2, "b").exec();

        tester.target("/e4_request_encoder/xyz")
                .queryParam("include", "[\"id\",\"cVarchar\"]")
                .queryParam("sort", "id")
                .get()
                .wasOk().bodyEquals(1, "{\"suffix\":\"xyz\"}");
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
        public DataResponse<E4> getWithPaginationStage(@Context UriInfo uriInfo) {
            return Ag.service(config)
                    .select(E4.class)
                    .uri(uriInfo)
                    .stage(SelectStage.APPLY_SERVER_PARAMS,
                            c -> RESOURCE_ENTITY_IS_FILTERED = c.getEntity().isFiltered())
                    .stage(SelectStage.ASSEMBLE_QUERY,
                            c -> QUERY_PAGE_SIZE = CayenneProcessor.getCayenneEntity(c.getEntity()).getSelect().getPageSize())
                    .get();
        }

        @GET
        @Path("e4_request_encoder/{suffix}")
        // typical use case tested here is an EncoderFilter that depends on request parameters
        public DataResponse<E4> getWithRequestEncoder(@Context UriInfo uriInfo, @PathParam("suffix") String suffix) {

            EntityEncoderFilter filter = EntityEncoderFilter.forEntity(E4.class)
                    .encoder((p, o, out, e) -> {
                        out.writeStartObject();
                        out.writeObjectField("suffix", suffix);
                        out.writeEndObject();
                        return true;
                    })
                    .build();

            return Ag.service(config)
                    .select(E4.class)
                    .entityEncoderFilter(filter)
                    .uri(uriInfo)
                    .get();
        }
    }
}
