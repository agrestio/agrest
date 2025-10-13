package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

import java.time.LocalDateTime;

public class ExpIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class)
            .build();

    @Test
    public void map() {

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
    public void map_Params() {

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
    public void bare() {

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
    public void list() {

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
    public void list_Params() {

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
    public void list_Params_Repeating() {

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
    public void list_Params_Timestamp() {

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
    public void id_Equals() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzzz").exec();

        tester.target("/e2")
                .queryParam("include", "id")
                .queryParam("exp", "id = 2")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void negativeNumeric() {
        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzzz").exec();

        tester.target("/e2")
                .queryParam("include", "id")
                .queryParam("exp", "id > -1")
                .queryParam("order", "name asc")
                .get()
                .wasOk().bodyEquals(3, "{\"id\":1}", "{\"id\":2}", "{\"id\":3}");
    }

    @Test
    public void toOne_Equals() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzzz").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(6, "yyy", 3)
                .values(8, "yyy", 1)
                .values(9, "zzz", 2).exec();

        tester.target("/e3")
                .queryParam("include", "id")
                .queryParam("exp", "e2 = 3")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":6}");
    }

    @Test
    public void toOne_Id_Equals() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzzz").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(6, "yyy", 3)
                .values(8, "yyy", 1)
                .values(9, "zzz", 2).exec();

        tester.target("/e3")
                .queryParam("include", "id")
                .queryParam("exp", "e2.id = 3")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":6}");
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
                .queryParam("exp", "{\"exp\":\"e2 in $ids\",\"params\":{\"ids\": [3, 4]}}")
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
                .queryParam("exp", "{\"exp\":\"e2 not in $ids\",\"params\":{\"ids\": [1, 2]}}")
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
                .queryParam("exp", "{\"exp\":\"e2+.name = null\"}")
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
                .queryParam("exp", "{\"exp\":\"e2+ = null\"}")
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
                .queryParam("exp", "{\"exp\":\"e3s+ = null\"}")
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
                .queryParam("exp", "{\"exp\":\"name in ($n1, $n2)\",\"params\":{\"n1\":\"zzz\",\"n2\":\"xxx\"}}")
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
                .queryParam("exp", "{\"exp\":\"e2.name in ($n1, $n2)\",\"params\":{\"n1\":\"zzz\",\"n2\":\"xxx\"}}")
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
                .queryParam("exp", "{\"exp\":\"e2 not in ($id)\",\"params\":{\"id\":1}}")
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
                .queryParam("exp", "{\"exp\":\"e2 not in ($id1, $id2)\",\"params\":{\"id1\":1,\"id2\":3}}")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":9}");
    }

    @Test
    public void like_MatchSingleQuote() {

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
    public void like_MatchDoubleQuote() {

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
    public void like_SingleChar_Pattern() {

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
    public void like_MultiChar_Pattern() {

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
    public void like_SingleChar_Pattern_Escape() {

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
    public void exists_Path_Relationship() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "qwe")
                .values(2, "try")
                .exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(1, "xxx", 1)
                .values(2, "yxy", 2)
                .values(3, "y_y", 2)
                .values(4, "y_ay", null)
                .exec();

        tester.target("/e3")
                .queryParam("include", "id")
                .queryParam("exp", "exists e2")
                .queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyEquals(3, "{\"id\":1}", "{\"id\":2}", "{\"id\":3}");
    }

    @Test
    public void exists_Path_NoRelationship() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "qwe")
                .values(2, "try")
                .exec();

        tester.e3().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yxy")
                .values(3, null)
                .exec();

        tester.target("/e3")
                .queryParam("include", "id")
                .queryParam("exp", "exists name")
                .queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"id\":1}", "{\"id\":2}");
    }

    @Test
    public void like_MultiChar_Pattern_Escape() {

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

    @Test
    public void path_Alias() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(4, "aaa", 1)
                .values(5, "bbb", 2)
                .values(6, "ccc", 2)
                .values(7, "ddd", 2).exec();

        tester.target("/e2").queryParam("include", "id")
                .queryParam("exp", "{\"exp\":\"e3s#a1.name = $name1 and e3s#a2.name = $name2\"" +
                        ",\"params\":{\"name1\": \"bbb\", \"name2\": \"ccc\"}}")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":2}");
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
