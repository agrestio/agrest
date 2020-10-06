package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E22;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.query.SelectById;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class GET_EntityOverlay_PerRequestIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E4.class, E22.class};
    }

    @Test
    public void test_DefaultIncludes() {

        e4().insertColumns("id", "c_varchar").values(2, "a").values(4, "b").exec();

        Response r = target("/e4/xyz")
                .queryParam("sort", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(2, "{\"id\":2,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null," +
                "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"a_x\",\"fromRequest\":\"xyz\",\"objectProperty\":\"a$\"}," +
                "{\"id\":4,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null," +
                "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"b_x\",\"fromRequest\":\"xyz\",\"objectProperty\":\"b$\"}");
    }

    @Test
    public void test_Includes() {

        e4().insertColumns("id", "c_varchar").values(2, "a").values(4, "b").exec();

        Response r = target("/e4/xyz")
                .queryParam("sort", "id")
                .queryParam("include", "[\"id\",\"cVarchar\",\"fromRequest\"]")
                .request()
                .get();

        onSuccess(r).bodyEquals(2, "{\"id\":2,\"cVarchar\":\"a_x\",\"fromRequest\":\"xyz\"}," +
                "{\"id\":4,\"cVarchar\":\"b_x\",\"fromRequest\":\"xyz\"}");
    }

    @Test
    public void test_Overlay_NoReaderCaching() {

        e4().insertColumns("id").values(2).values(4).exec();

        Response r1 = target("/e4/xyz")
                .queryParam("sort", "id")
                .queryParam("include", "fromRequest")
                .request()
                .get();

        onSuccess(r1).bodyEquals(2, "{\"fromRequest\":\"xyz\"},{\"fromRequest\":\"xyz\"}");


        // at some point in time readers were cached, so changing the URL parameter would still return the old result
        Response r2 = target("/e4/abc")
                .queryParam("sort", "id")
                .queryParam("include", "fromRequest")
                .request()
                .get();

        onSuccess(r2).bodyEquals(2, "{\"fromRequest\":\"abc\"},{\"fromRequest\":\"abc\"}");
    }

    @Test
    public void test_OverlayedRelationship() {

        e4().insertColumns("id", "c_varchar").values(2, "a").values(4, "b").exec();
        e22().insertColumns("id", "name")
                .values(1, "a")
                .values(2, "b")
                .values(3, "c").exec();

        Response r = target("/e4/xyz")
                .queryParam("include", "[\"id\",\"cVarchar\",\"fromRequest\",\"dynamicRelationship\"]")
                .queryParam("sort", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(2, "{\"id\":2,\"cVarchar\":\"a_x\",\"dynamicRelationship\":{\"id\":2,\"name\":\"b\",\"prop1\":null,\"prop2\":null},\"fromRequest\":\"xyz\"}," +
                "{\"id\":4,\"cVarchar\":\"b_x\",\"dynamicRelationship\":null,\"fromRequest\":\"xyz\"}");
    }

    @Test
    public void test_OverlayedRelationship_CayenneExpOnParent() {

        e4().insertColumns("id", "c_varchar").values(2, "a").values(4, "b").exec();
        e22().insertColumns("id", "name")
                .values(1, "a")
                .values(2, "b")
                .values(3, "c").exec();

        Response r = target("/e4/xyz")
                .queryParam("cayenneExp", "id = 2")
                .queryParam("include", "[\"id\",\"dynamicRelationship\"]")
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":2,\"dynamicRelationship\":{\"id\":2,\"name\":\"b\",\"prop1\":null,\"prop2\":null}}");
    }

    @Ignore("A relationship that is a child of dynamic relationship fails to resolve properly. We must use " +
            "read-from-parent resolver for anything hanging off of a dynamic relationship instead of using built-in Cayenne resolvers")
    @Test
    public void test_OverlayedRelationship_CayenneExpOnParent_Nested() {

        e4().insertColumns("id", "c_varchar").values(2, "a").values(4, "b").exec();

        e2().insertColumns("id_", "name")
                .values(1, "a2")
                .values(2, "b2")
                .values(3, "c2").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(1, "a", 1)
                .values(2, "b", 1)
                .values(3, "c", 1).exec();

        Response r = target("/e4_2")
                .queryParam("cayenneExp", "id = 2")
                .queryParam("include", "[\"id\",\"dynamicRelationship.e2\"]")
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":2,\"dynamicRelationship\":{\"e2\":{\"id\":2,\"name\":\"b2\"}}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        private static <T extends Persistent> T findMatching(Class<T> type, Persistent p) {
            return SelectById.query(type, Cayenne.pkForObject(p)).selectOne(p.getObjectContext());
        }

        @GET
        @Path("e4/{suffix}")
        // typical use case tested here is an AgEntityOverlay that depends on request parameters
        public DataResponse<E4> get(@Context UriInfo uriInfo, @PathParam("suffix") String suffix) {

            AgEntityOverlay<E4> overlay = AgEntity.overlay(E4.class)
                    // 1. Request-specific attribute
                    .redefineAttribute("fromRequest", String.class, e4 -> suffix)
                    // 2. Object property previously unknown to Ag
                    .redefineAttribute("objectProperty", String.class, e4 -> e4.getDerived())
                    // 3. Changing output of the existing property
                    .redefineAttribute("cVarchar", String.class, e4 -> e4.getCVarchar() + "_x")
                    // 4. Dynamic relationship
                    .redefineToOne("dynamicRelationship", E22.class, e4 -> findMatching(E22.class, e4));

            return Ag.service(config)
                    .select(E4.class)
                    .entityOverlay(overlay)
                    .uri(uriInfo)
                    .get();
        }

        @GET
        @Path("e4_2")
        public DataResponse<E4> getE4_WithE3(@Context UriInfo uriInfo) {

            AgEntityOverlay<E4> overlay = AgEntity.overlay(E4.class)
                    // dynamic relationship
                    .redefineToOne("dynamicRelationship", E3.class, e4 -> findMatching(E3.class, e4));

            return Ag.service(config)
                    .select(E4.class)
                    .entityOverlay(overlay)
                    .uri(uriInfo)
                    .get();
        }
    }
}
