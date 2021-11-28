package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.EntityUpdate;
import io.agrest.cayenne.cayenne.main.E10;
import io.agrest.cayenne.cayenne.main.E11;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E22;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.query.SelectById;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.List;

public class PUT_EntityOverlay_PerRequestIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class, E10.class, E11.class, E22.class)
            .build();

    @Test
    public void test_RequestOverlaidProperties_ConstrainedEntity() {

        tester.e10().insertColumns("id", "c_int").values(2, 5).values(4, 8).exec();
        tester.e22().insertColumns("id", "name").values(1, "a").values(2, "b").exec();

        tester.target("/e10/xyz/2")
                .queryParam("include", "[\"id\",\"cInt\",\"fromRequest\",\"dynamicRelationship.name\"]")
                .put("{\"cInt\":88}")
                .wasOk()
                .bodyEquals(
                        1,
                        "{\"id\":2,\"cInt\":89,\"dynamicRelationship\":{\"name\":\"b\"},\"fromRequest\":\"xyz\"}");
    }

    @Test
    public void test_DefaultIncludes() {

        tester.e4().insertColumns("id", "c_varchar").values(2, "a").values(4, "b").exec();

        tester.target("/e4/xyz")
                .queryParam("sort", "id")
                // "objectProperty" is not writable, and should be ignored
                .put("[{\"id\":2,\"cBoolean\":true,\"cVarchar\":\"c\",\"objectProperty\":\"xxx$\"}," +
                        "{\"id\":4,\"cBoolean\":false,\"cVarchar\":\"d\",\"objectProperty\":\"yyy$\"}]")
                .wasOk()
                .bodyEquals(2, "{\"id\":2,\"cBoolean\":true,\"cDate\":null,\"cDecimal\":null,\"cInt\":null," +
                                "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"c_x\",\"fromRequest\":\"xyz\",\"objectProperty\":\"c$\"}",
                        "{\"id\":4,\"cBoolean\":false,\"cDate\":null,\"cDecimal\":null,\"cInt\":null," +
                                "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"d_x\",\"fromRequest\":\"xyz\",\"objectProperty\":\"d$\"}");
    }

    @Test
    public void test_OverlaidNestedExclude() {

        tester.e2().insertColumns("id_", "name", "address").values(1, "N", "A").exec();
        tester.e3().insertColumns("id_", "name", "phone_number", "e2_id")
                .values(1, "N", "P", 1)
                .exec();

        tester.target("/e2/1")
                .queryParam("include", "e3s")
                .put("{\"name\":\"Nn\",\"address\":\"Aa\"}")
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"e3s\":[{\"id\":1,\"name\":\"N\"}],\"name\":\"Nn\"}");

        tester.e2().matcher().eq("id_", 1).eq("name", "Nn").eq("address", "A").assertOneMatch();
    }

    @Test
    public void test_OverlaidExclude() {

        tester.e2().insertColumns("id_", "name", "address").values(1, "N", "A").exec();

        tester.target("/e2/1")
                .put("{\"name\":\"Nn\",\"address\":\"Aa\"}")
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"name\":\"Nn\"}");

        tester.e2().matcher().eq("id_", 1).eq("name", "Nn").eq("address", "A").assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        private static <T extends Persistent> T findMatching(Class<T> type, Persistent p) {
            return SelectById.query(type, Cayenne.pkForObject(p)).selectOne(p.getObjectContext());
        }

        @PUT
        @Path("e10/{suffix}/{id}")
        public DataResponse<E10> putE10(
                @Context UriInfo uriInfo,
                @PathParam("suffix") String suffix,
                @PathParam("id") Integer id,
                EntityUpdate<E10> data) {

            AgEntityOverlay<E10> overlay = AgEntity.overlay(E10.class)
                    // 1. Request-specific attribute
                    .redefineAttribute("fromRequest", String.class, e4 -> suffix)
                    // 3. Changing output of the existing property
                    .redefineAttribute("cInt", Integer.class, e10 -> e10.getCInt() + 1)
                    // 4. Dynamic relationship
                    .redefineToOne("dynamicRelationship", E22.class, e10 -> findMatching(E22.class, e10));

            return Ag.service(config)
                    .createOrUpdate(E10.class)
                    .entityOverlay(overlay)
                    .id(id)
                    .uri(uriInfo)
                    .syncAndSelect(data);
        }

        @PUT
        @Path("e4/{suffix}")
        public DataResponse<E4> putE4(
                @Context UriInfo uriInfo,
                @PathParam("suffix") String suffix,
                List<EntityUpdate<E4>> data) {

            AgEntityOverlay<E4> overlay = AgEntity.overlay(E4.class)
                    // 1. Request-specific attribute
                    .redefineAttribute("fromRequest", String.class, e4 -> suffix)
                    // 2. Object property previously unknown to Ag
                    .redefineAttribute("objectProperty", String.class, E4::getDerived)
                    // 3. Changing output of the existing property
                    .redefineAttribute("cVarchar", String.class, e4 -> e4.getCVarchar() + "_x")
                    // 4. Dynamic relationship
                    .redefineToOne("dynamicRelationship", E22.class, e4 -> findMatching(E22.class, e4));

            return Ag.service(config)
                    .createOrUpdate(E4.class)
                    .entityOverlay(overlay)
                    .uri(uriInfo)
                    .syncAndSelect(data);
        }

        @PUT
        @Path("e2/{id}")
        public DataResponse<E2> putE2_With_exclude(
                @Context UriInfo uriInfo,
                @PathParam("id") Integer id,
                EntityUpdate<E2> data) {

            AgEntityOverlay<E2> e2Overlay = AgEntity.overlay(E2.class).exclude("address");
            AgEntityOverlay<E3> e3Overlay = AgEntity.overlay(E3.class).exclude("phoneNumber");

            return Ag.service(config)
                    .createOrUpdate(E2.class)
                    .entityOverlay(e2Overlay)
                    .entityOverlay(e3Overlay)
                    .uri(uriInfo)
                    .id(id)
                    .syncAndSelect(data);
        }
    }
}