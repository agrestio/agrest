package io.agrest.cayenne;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.time.LocalDateTime;

public class GET_ExpIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class)
            .build();

    @Test
    public void testMap() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        tester.target("/e2").queryParam("include", "id")
                .queryParam("exp", "{\"exp\":\"name = 'yyy'\"}")
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
                .queryParam("exp", "{\"exp\":\"name = $n\",\"params\":{\"n\":\"xxx\"}}")
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
                .queryParam("exp", "name = 'yyy'")
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
                .queryParam("exp", "[\"name = 'yyy'\"]")
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
                .queryParam("exp", "[\"name = $b\", \"xxx\"]")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":1}");
    }

    @Test
    @DisplayName("positional binding of repeating name")
    public void testList_Params_Repeating() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        tester.target("/e2")
                .queryParam("include", "id")
                .queryParam("exp", "[\"name = $b or name = $b or id = $a\", \"xxx\", 1]")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":1}");
    }

    @Test
    public void testList_Params_Timestamp() {

        tester.e4().insertColumns("id", "c_timestamp")
                .values(1, LocalDateTime.of(2017, 2, 3, 5, 6, 7))
                .values(4, LocalDateTime.of(2016, 2, 4, 5, 6, 8)).exec();

        tester.target("/e4")
                .queryParam("include", "id")
                .queryParam("exp", "[\"cTimestamp > $ts\", \"2016-06-01T00:00:00\"]")
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
                .queryParam("exp", "{\"exp\":\"e2 in $ids\",\"params\":{\"ids\": [3, 4]}}")
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
                .queryParam("exp", "{\"exp\":\"e2 not in $ids\",\"params\":{\"ids\": [1, 2]}}")
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
                .queryParam("exp", "{\"exp\":\"e2+.name = null\"}")
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
                .queryParam("exp", "{\"exp\":\"e2+ = null\"}")
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
                .queryParam("exp", "{\"exp\":\"e3s+ = null\"}")
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
                .queryParam("exp", "{\"exp\":\"name in ($n1, $n2)\",\"params\":{\"n1\":\"zzz\",\"n2\":\"xxx\"}}")
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
                .queryParam("exp", "{\"exp\":\"e2.name in ($n1, $n2)\",\"params\":{\"n1\":\"zzz\",\"n2\":\"xxx\"}}")
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
                .queryParam("exp", "{\"exp\":\"e2 not in ($id)\",\"params\":{\"id\":1}}")
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
                .queryParam("exp", "{\"exp\":\"e2 not in ($id1, $id2)\",\"params\":{\"id1\":1,\"id2\":3}}")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":9}");
    }

    @Test
    public void testLike_MatchSingleQuote() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yxy")
                .values(3, "y'y")
                .values(4, "yyy")
                .exec();

        tester.target("/e2")
                .queryParam("include", "id")
                .queryParam("exp", "name like 'y\\'y'")
                .queryParam("sort", "id")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":3}");
    }

    @Test
    public void testLike_MatchDoubleQuote() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yxy")
                .values(3, "y\"y")
                .values(4, "yyy")
                .exec();

        tester.target("/e2")
                .queryParam("include", "id")
                .queryParam("exp", "name like 'y\\\"y'")
                .queryParam("sort", "id")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":3}");
    }

    @Test
    public void testLike_SingleChar_Pattern() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yxy")
                .values(3, "yxxy")
                .values(4, "yyy")
                .exec();

        tester.target("/e2")
                .queryParam("include", "id")
                .queryParam("exp", "name like 'y_y'")
                .queryParam("sort", "id")
                .get()
                .wasOk().bodyEquals(2, "{\"id\":2}", "{\"id\":4}");
    }

    @Test
    public void testLike_MultiChar_Pattern() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yxy")
                .values(3, "yxxy")
                .values(4, "yyy")
                .exec();

        tester.target("/e2")
                .queryParam("include", "id")
                .queryParam("exp", "name like 'y%y'")
                .queryParam("sort", "id")
                .get()
                .wasOk().bodyEquals(3, "{\"id\":2}", "{\"id\":3}", "{\"id\":4}");
    }

    @Test
    public void testLike_SingleChar_Pattern_Escape() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yxy")
                .values(3, "y_y")
                .values(4, "y_ay")
                .values(5, "yyy")
                .exec();

        tester.target("/e2")
                .queryParam("include", "id")
                .queryParam("exp", "name like 'y@__y' escape '@'")
                .queryParam("sort", "id")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":4}");
    }

    @Test
    public void testLike_MultiChar_Pattern_Escape() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yxy")
                .values(3, "y%y")
                .values(4, "y%ay")
                .values(5, "yyy")
                .exec();

        tester.target("/e2")
                .queryParam("include", "id")
                .queryParam("exp", "name like 'y@%%y' escape '@'")
                .queryParam("sort", "id")
                .get()
                .wasOk().bodyEquals(2, "{\"id\":3}", "{\"id\":4}");
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

        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E4.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }
    }
}
