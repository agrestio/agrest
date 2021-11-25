package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.EntityUpdate;
import io.agrest.cayenne.cayenne.main.E10;
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

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class PUT_EntityOverlay_PerRequestIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class, E10.class, E22.class)
            .build();

    @Test
    public void test_RequestOverlaidProperties_ConstrainedEntity() {

        tester.e10().insertColumns("id", "c_int").values(2, 5).values(4, 8).exec();
        tester.e22().insertColumns("id", "name").values(1, "a").values(2, "b").exec();

        tester.target("/e10/xyz/2")
                .queryParam("include", "[\"id\",\"cInt\",\"fromRequest\",\"dynamicRelationship.name\"]")
                .put("{\"id\":2,\"cInt\":88}")
                .wasOk()
                .bodyEquals(
                        1,
                        "{\"id\":2,\"cInt\":89,\"dynamicRelationship\":{\"name\":\"b\"},\"fromRequest\":\"xyz\"}");
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
        public DataResponse<E10> putE10(@Context UriInfo uriInfo, @PathParam("suffix") String suffix, @PathParam("id") String id, EntityUpdate<E10> data) {

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

        @GET
        @Path("e2")
        public DataResponse<E2> getE2_With_exclude(@Context UriInfo uriInfo) {

            AgEntityOverlay<E2> e2Overlay = AgEntity.overlay(E2.class).exclude("address");
            AgEntityOverlay<E3> e3Overlay = AgEntity.overlay(E3.class).exclude("phoneNumber");

            return Ag.service(config)
                    .select(E2.class)
                    .entityOverlay(e2Overlay)
                    .entityOverlay(e3Overlay)
                    .uri(uriInfo)
                    .get();
        }
    }
}
