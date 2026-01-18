package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

@Deprecated(forRemoval = false)
public class CayenneExpIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class)
            .build();

    @Test
    public void map() {

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
    public void map_Params() {

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
    public void bare() {

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
    public void list() {

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
    public void list_Params() {

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
    public void in_Array() {

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
    public void notIn_Array() {

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
    public void outer() {

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
    public void outer_Relationship() {

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
    public void outer_To_Many_Relationship() {

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
    public void in_TwoObjects() {

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
    public void in_TwoRelatedObjects() {

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
    public void notIn_ById() {

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
    public void notIn_By2Ids() {

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
