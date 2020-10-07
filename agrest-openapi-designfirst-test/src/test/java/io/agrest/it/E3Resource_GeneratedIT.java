package io.agrest.it;


import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.swagger.api.v1.service.E3Resource;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

public class E3Resource_GeneratedIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(E3Resource.class)
            .entities(E2.class, E3.class)
            .build();

    @Test
    public void testGET_ById_Include() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/v1/e3/8").queryParam("include", "e2.id").get()
                .wasOk()
                .bodyEquals(1, "{\"id\":8,\"e2\":{\"id\":1},\"name\":\"yyy\",\"phoneNumber\":null}");

        tester.target("/v1/e3/8").queryParam("include", "e2.name").get()
                .wasOk()
                .bodyEquals(1, "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\",\"phoneNumber\":null}");
    }

    @Test
    public void testGET_Include_Sort_Dir() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(9, "zzz", 1)
                .values(8, "yyy", 1).exec();

        tester.target("/v1/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .queryParam("dir", "ASC")
                .get()
                .wasOk().bodyEquals(2, "{\"id\":8,\"e2\":{\"id\":1}}", "{\"id\":9,\"e2\":{\"id\":1}}");

        tester.target("/v1/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .queryParam("dir", "DESC")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"id\":9,\"e2\":{\"id\":1}}", "{\"id\":8,\"e2\":{\"id\":1}}");
    }

    @Test
    public void testGET_Start_Limit() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(10, "zzz", 1)
                .values(9, "zzz", 1)
                .values(8, "yyy", 1)
                .values(11, "zzz", 1).exec();

        tester.target("/v1/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .queryParam("start", "1")
                .queryParam("limit", "2")
                .get()
                .wasOk()
                .bodyEquals(4,
                        "{\"id\":9,\"e2\":{\"id\":1}}",
                        "{\"id\":10,\"e2\":{\"id\":1}}");
    }

    @Test
    public void testGET_CayenneExp() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzzz").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(6, "yyy", 3)
                .values(8, "yyy", 1)
                .values(9, "zzz", 2).exec();

        tester.target("/v1/e3")
                .queryParam("include", "id")
                .queryParam("cayenneExp", "{\"exp\":\"e2 in $ids\",\"params\":{\"ids\": [3, 4]}}")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":6}");
    }

    @Test
    public void testGET_RelatedAll_Include() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/v1/e3/7/e2").queryParam("include", "id")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testPOST() {
        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.target("/v1/e3")
                .post("{\"e2\":8,\"name\":\"MM\"}")
                .wasCreated()
                .replaceId("XID").bodyEquals(1, "{\"id\":XID,\"name\":\"MM\",\"phoneNumber\":null}");
    }

    @Test
    public void testPUT_ById() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        tester.target("/v1/e3/3")
                .put("{\"id\":3,\"e2\":1}")
                .wasOk()
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("id_", 3).eq("e2_id", 1).assertOneMatch();
    }


    @Test
    public void testPUT_Bulk() {

        tester.e3().insertColumns("id_", "name")
                .values(5, "aaa")
                .values(4, "zzz")
                .values(2, "bbb")
                .values(6, "yyy").exec();

        String entity =
                "[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"},{\"id\":5,\"name\":\"111\"},{\"id\":2,\"name\":\"333\"}]";
        tester.target("/v1/e3/")
                .queryParam("exclude", "id")
                .queryParam("include", "name")
                .put(entity)
                .wasOk()
                // ordering must be preserved in response, so comparing with request entity
                .bodyEquals(4,
                        "{\"name\":\"yyy\"}",
                        "{\"name\":\"zzz\"}",
                        "{\"name\":\"111\"}",
                        "{\"name\":\"333\"}");
    }

    @Test
    public void testPUT_Relate() {

        tester.e2().insertColumns("id_", "name")
                .values(24, "xxx").exec();

        tester.e3().insertColumns("id_", "name")
                .values(7, "zzz")
                .values(8, "yyy").exec();

        // PUT with empty body ... how bad is that?
        tester.target("/v1/e3/8/e2/24").put("")
                .wasOk()
                .bodyEquals(1, "{\"id\":24,\"address\":null,\"name\":\"xxx\"}");

        tester.e3().matcher().eq("e2_id", 24).eq("name", "yyy").assertOneMatch();
    }

    @Test
    public void testDELETE_UnrelateById() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/v1/e3/9/e2/1").delete()
                .wasOk()
                .bodyEquals("{\"success\":true}");

        // this delete is really "unrelate", so the records will be there, but their FKs will be set to null
        tester.e3().matcher().assertMatches(3);
        tester.e3().matcher().eq("e2_id", 1).assertOneMatch();
    }

    @Test
    public void testDELETE_UnrelateAll() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/v1/e3/9/e2").delete().wasOk().bodyEquals("{\"success\":true}");

        // this delete is really "unrelate", so the records will be there, but their FKs will be set to null
        tester.e3().matcher().assertMatches(3);
        tester.e3().matcher().eq("e2_id", 1).assertOneMatch();
    }
}
