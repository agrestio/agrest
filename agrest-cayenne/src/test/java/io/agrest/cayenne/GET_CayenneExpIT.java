package io.agrest.cayenne;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Deprecated
public class GET_CayenneExpIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class)
            .build();

    @Test
    public void testMap() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        tester.target("/e2").queryParam("include", "id")
                .queryParam("cayenneExp", "{\"exp\":\"name = 'yyy'\"}")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testMap_Params() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        tester.target("/e2").queryParam("include", "id")
                .queryParam("cayenneExp", "{\"exp\":\"name = $n\",\"params\":{\"n\":\"xxx\"}}")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1}");
    }

    @Test
    public void testBare() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        tester.target("/e2")
                .queryParam("include", "id")
                .queryParam("cayenneExp", "name = 'yyy'")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testList() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        tester.target("/e2")
                .queryParam("include", "id")
                .queryParam("cayenneExp", "[\"name = 'yyy'\"]")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testList_Params() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        tester.target("/e2")
                .queryParam("include", "id")
                .queryParam("cayenneExp", "[\"name = $b\", \"xxx\"]")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":1}");
    }

    @Test
    public void testIn_Array() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzzz").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(6, "yyy", 3)
                .values(8, "yyy", 1)
                .values(9, "zzz", 2).exec();

        tester.target("/e3").queryParam("include", "id")
                .queryParam("cayenneExp", "{\"exp\":\"e2 in $ids\",\"params\":{\"ids\": [3, 4]}}")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":6}");
    }

    @Test
    public void testNotIn_Array() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 2).exec();

        tester.target("/e3")
                .queryParam("include", "id")
                .queryParam("cayenneExp", "{\"exp\":\"e2 not in $ids\",\"params\":{\"ids\": [1, 2]}}")
                .get()
                .wasOk().bodyEquals(0, "");
    }

    @Test
    public void testOuter() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "A", 1)
                .values(9, "B", null).exec();

        tester.target("/e3")
                .queryParam("include", "id")
                .queryParam("cayenneExp", "{\"exp\":\"e2+.name = null\"}")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":9}");
    }

    @Test
    public void testOuter_Relationship() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "A", 1)
                .values(9, "B", null).exec();

        tester.target("/e3")
                .queryParam("include", "id")
                .queryParam("cayenneExp", "{\"exp\":\"e2+ = null\"}")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":9}");
    }

    @Test
    public void testOuter_To_Many_Relationship() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "A", 1)
                .values(9, "B", null).exec();

        tester.target("/e2")
                .queryParam("include", "id")
                .queryParam("cayenneExp", "{\"exp\":\"e3s+ = null\"}")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testIn_TwoObjects() {

        tester.e3().insertColumns("id_", "name")
                .values(8, "yyy")
                .values(9, "zzz").exec();

        tester.target("/e3")
                .queryParam("include", "id")
                .queryParam("cayenneExp", "{\"exp\":\"name in ($n1, $n2)\",\"params\":{\"n1\":\"zzz\",\"n2\":\"xxx\"}}")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":9}");
    }

    @Test
    public void testIn_TwoRelatedObjects() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 2).exec();

        tester.target("/e3")
                .queryParam("include", "id")
                .queryParam("cayenneExp", "{\"exp\":\"e2.name in ($n1, $n2)\",\"params\":{\"n1\":\"zzz\",\"n2\":\"xxx\"}}")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":8}");
    }

    @Test
    public void testNotIn_ById() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 2).exec();

        tester.target("/e3").queryParam("include", "id")
                .queryParam("cayenneExp", "{\"exp\":\"e2 not in ($id)\",\"params\":{\"id\":1}}")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":9}");
    }

    @Test
    public void testNotIn_By2Ids() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 2).exec();

        tester.target("/e3")
                .queryParam("include", "id")
                .queryParam("cayenneExp", "{\"exp\":\"e2 not in ($id1, $id2)\",\"params\":{\"id1\":1,\"id2\":3}}")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":9}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E2.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e3")
        public DataResponse<E3> getE3(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E3.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }
    }
}
