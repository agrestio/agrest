package io.agrest.cayenne;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E10;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E22;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.query.SelectById;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class GET_EntityOverlay_PerRequestIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class, E22.class)
            .entitiesAndDependencies(E10.class)
            .build();

    @Test
    public void test_DefaultIncludes() {

        tester.e4().insertColumns("id", "c_varchar").values(2, "a").values(4, "b").exec();

        tester.target("/e4/xyz")
                .queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"id\":2,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null," +
                        "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"a_x\",\"fromRequest\":\"xyz\",\"objectProperty\":\"a$\"}," +
                        "{\"id\":4,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null," +
                        "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"b_x\",\"fromRequest\":\"xyz\",\"objectProperty\":\"b$\"}");
    }

    @Test
    public void test_Includes() {

        tester.e4().insertColumns("id", "c_varchar").values(2, "a").values(4, "b").exec();

        tester.target("/e4/xyz")
                .queryParam("sort", "id")
                .queryParam("include", "[\"id\",\"cVarchar\",\"fromRequest\"]")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"id\":2,\"cVarchar\":\"a_x\",\"fromRequest\":\"xyz\"}," +
                        "{\"id\":4,\"cVarchar\":\"b_x\",\"fromRequest\":\"xyz\"}");
    }

    @Test
    public void test_Overlay_NoReaderCaching() {

        tester.e4().insertColumns("id").values(2).values(4).exec();

        tester.target("/e4/xyz")
                .queryParam("sort", "id")
                .queryParam("include", "fromRequest")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"fromRequest\":\"xyz\"},{\"fromRequest\":\"xyz\"}");

        // at some point in time readers were cached, so changing the URL parameter would still return the old result
        tester.target("/e4/abc")
                .queryParam("sort", "id")
                .queryParam("include", "fromRequest")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"fromRequest\":\"abc\"},{\"fromRequest\":\"abc\"}");
    }

    @Test
    public void test_RequestOverlaidProperties_ConstrainedEntity() {

        tester.e10().insertColumns("id", "c_int").values(2, 5).values(4, 8).exec();
        tester.e22().insertColumns("id", "name")
                .values(1, "a")
                .values(2, "b")
                .values(3, "c").exec();

        tester.target("/e10/xyz")
                .queryParam("include", "[\"id\",\"cInt\",\"fromRequest\",\"dynamicRelationship\"]")
                .queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyEquals(
                        2,
                        "{\"id\":2,\"cInt\":6,\"dynamicRelationship\":{\"id\":2,\"name\":\"b\",\"prop1\":null,\"prop2\":null},\"fromRequest\":\"xyz\"}," +
                                "{\"id\":4,\"cInt\":9,\"dynamicRelationship\":null,\"fromRequest\":\"xyz\"}");
    }

    @Test
    public void test_OverlaidRelationship() {

        tester.e4().insertColumns("id", "c_varchar").values(2, "a").values(4, "b").exec();
        tester.e22().insertColumns("id", "name")
                .values(1, "a")
                .values(2, "b")
                .values(3, "c").exec();

        tester.target("/e4/xyz")
                .queryParam("include", "[\"id\",\"cVarchar\",\"fromRequest\",\"dynamicRelationship\"]")
                .queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyEquals(
                        2,
                        "{\"id\":2,\"cVarchar\":\"a_x\",\"dynamicRelationship\":{\"id\":2,\"name\":\"b\",\"prop1\":null,\"prop2\":null},\"fromRequest\":\"xyz\"}," +
                                "{\"id\":4,\"cVarchar\":\"b_x\",\"dynamicRelationship\":null,\"fromRequest\":\"xyz\"}");
    }

    @Test
    public void test_OverlaidRelationship_ExpOnParent() {

        tester.e4().insertColumns("id", "c_varchar").values(2, "a").values(4, "b").exec();
        tester.e22().insertColumns("id", "name")
                .values(1, "a")
                .values(2, "b")
                .values(3, "c").exec();

        tester.target("/e4/xyz")
                .queryParam("exp", "id = 2")
                .queryParam("include", "[\"id\",\"dynamicRelationship\"]")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":2,\"dynamicRelationship\":{\"id\":2,\"name\":\"b\",\"prop1\":null,\"prop2\":null}}");
    }

    @Test
    public void test_OverlaidRelationship_ExpOnParent_NestedToOne() {

        tester.e4().insertColumns("id", "c_varchar").values(2, "a").values(4, "b").exec();

        tester.e2().insertColumns("id_", "name")
                .values(1, "a2")
                .values(2, "b2")
                .values(3, "c2").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(1, "a", 1)
                .values(2, "b", 1)
                .values(3, "c", 1).exec();

        tester.target("/e4_2")
                .queryParam("exp", "id = 2")
                .queryParam("include", "[\"id\",\"dynamicRelationship.e2\"]")
                .get().wasOk()
                .bodyEquals(1, "{\"id\":2,\"dynamicRelationship\":{\"e2\":{\"id\":1,\"address\":null,\"name\":\"a2\"}}}");
    }

    @Test
    public void test_OverlaidNestedExclude() {

        tester.e2().insertColumns("id_", "name", "address").values(1, "N", "A").exec();
        tester.e3().insertColumns("id_", "name", "phone_number", "e2_id")
                .values(1, "N", "P", 1)
                .exec();

        tester.target("/e2")
                .queryParam("include", "e3s")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"e3s\":[{\"id\":1,\"name\":\"N\"}],\"name\":\"N\"}");
    }

    @Test
    public void test_OverlaidExclude() {

        tester.e2().insertColumns("id_", "name", "address").values(1, "N", "A").exec();

        tester.target("/e2")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"name\":\"N\"}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        private static <T extends Persistent> T findMatching(Class<T> type, Persistent p) {
            return SelectById.query(type, Cayenne.pkForObject(p)).selectOne(p.getObjectContext());
        }

        @GET
        @Path("e10/{suffix}")
        public DataResponse<E10> getE10(@Context UriInfo uriInfo, @PathParam("suffix") String suffix) {

            AgEntityOverlay<E10> overlay = AgEntity.overlay(E10.class)
                    // 1. Request-specific attribute
                    .redefineAttribute("fromRequest", String.class, e4 -> suffix)
                    // 3. Changing output of the existing property
                    .redefineAttribute("cInt", Integer.class, e10 -> e10.getCInt() + 1)
                    // 4. Dynamic relationship
                    .redefineToOne("dynamicRelationship", E22.class, e10 -> findMatching(E22.class, e10));

            return AgJaxrs
                    .select(E10.class, config)
                    .entityOverlay(overlay)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }

        @GET
        @Path("e4/{suffix}")
        public DataResponse<E4> getE4(@Context UriInfo uriInfo, @PathParam("suffix") String suffix) {

            AgEntityOverlay<E4> overlay = AgEntity.overlay(E4.class)
                    // 1. Request-specific attribute
                    .redefineAttribute("fromRequest", String.class, e4 -> suffix)
                    // 2. Object property previously unknown to Ag
                    .redefineAttribute("objectProperty", String.class, E4::getDerived)
                    // 3. Changing output of the existing property
                    .redefineAttribute("cVarchar", String.class, e4 -> e4.getCVarchar() + "_x")
                    // 4. Dynamic relationship
                    .redefineToOne("dynamicRelationship", E22.class, e4 -> findMatching(E22.class, e4));

            return AgJaxrs
                    .select(E4.class, config)
                    .entityOverlay(overlay)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }

        @GET
        @Path("e4_2")
        public DataResponse<E4> getE4_WithE3(@Context UriInfo uriInfo) {

            AgEntityOverlay<E4> overlay = AgEntity.overlay(E4.class)
                    // dynamic relationship
                    .redefineToOne("dynamicRelationship", E3.class, e4 -> findMatching(E3.class, e4));

            return AgJaxrs.select(E4.class, config)
                    .entityOverlay(overlay)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }

        @GET
        @Path("e2")
        public DataResponse<E2> getE2_With_exclude(@Context UriInfo uriInfo) {

            AgEntityOverlay<E2> e2Overlay = AgEntity.overlay(E2.class).readablePropFilter(p -> p.property("address", false));
            AgEntityOverlay<E3> e3Overlay = AgEntity.overlay(E3.class).readablePropFilter(p -> p.property("phoneNumber", false));

            return AgJaxrs.select(E2.class, config)
                    .entityOverlay(e2Overlay)
                    .entityOverlay(e3Overlay)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }
    }
}
