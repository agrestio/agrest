package io.agrest.j17;

import io.agrest.DataResponse;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.j17.junit.AgPojoTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

@BQTest
public class RecordGET_IT {

    @BQTestTool
    static final AgPojoTester tester = AgPojoTester.builder()
            .resources(Resource.class)
            .build();

    @Test
    public void testEmpty() {
        Response r = tester.target().path("/r11").request().get();
        JettyTester.assertOk(r)
                .assertContent("""
                        {"data":[],"total":0}""");
    }

    @Test
    public void test() {
        Response r = tester.target().path("/r12").request().get();
        JettyTester.assertOk(r)
                .assertContent("""
                        {"data":[{"id":1,"agAt1":"a1"},{"id":2,"agAt1":"a2"}],"total":2}""");
    }

    @Test
    public void testIncludes() {
        Response r = tester.target().path("/r12")
                .queryParam("include", "id", "agRel1", "agRel2")
                .request().get();
        JettyTester.assertOk(r)
                .assertContent("""
                        {"data":[\
                        {"id":1,"agRel1":{"agAt":1},"agRel2":[{"agAt":1}]},\
                        {"id":2,"agRel1":{"agAt":2},"agRel2":[{"agAt":2},{"agAt":3}]}\
                        ],"total":2}""");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("r11")
        public DataResponse<R1> r11(@Context UriInfo uriInfo) {
            return AgJaxrs.select(R1.class, config).getEmpty();
        }

        @GET
        @Path("r12")
        public DataResponse<R1> r12(@Context UriInfo uriInfo) {
            AgEntityOverlay<R1> r1o = AgEntity.overlay(R1.class)
                    .dataResolver(c -> List.of(
                            new R1(1L, "a1", LocalDate.of(2000, 1, 1), new R2(null, 1), List.of(new R3(null, 1)), null),
                            new R1(2L, "a2", LocalDate.of(2000, 1, 2), new R2(null, 2), List.of(new R3(null, 2), new R3(null, 3)), null)));

            return AgJaxrs.select(R1.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .entityOverlay(r1o)
                    .get();
        }
    }

    public record R1(
            @AgId long agId,
            @AgAttribute(writable = false) String agAt1,
            @AgAttribute(readable = false) LocalDate agAt2,
            @AgRelationship R2 agRel1,
            @AgRelationship List<R3> agRel2,
            Object nonAg) {
    }

    public record R2(Object nonAg, @AgAttribute int agAt) {
    }

    public record R3(Object nonAg, @AgAttribute int agAt) {
    }
}
