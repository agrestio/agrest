package io.agrest.it;


import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.swagger.api.v1.service.E2Resource;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

public class E2Resource_GeneratedIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(E2Resource.class)
            .entities(E2.class, E3.class)
            .build();

    @Test
    public void testGET_ById_Include() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/v1/e2/1")
                .queryParam("include", "e3s.id")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"address\":null,\"e3s\":[{\"id\":8},{\"id\":9}],\"name\":\"xxx\"}");
    }

    @Test
    public void testGET_Exp() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        tester.target("/v1/e2")
                .queryParam("include", "id")
                .queryParam("exp", "name = 'yyy'")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":2}");
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

        tester.target("/v1/e2/1/e3s")
                .queryParam("include", "id")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"id\":8},{\"id\":9}");
    }


    @Test
    public void testGET_RelatedById_Include() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/v1/e2/1/e3s/8")
                .queryParam("include", "id")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":8}");
    }

    @Test
    public void testPOST_IncludeExclude() {

        tester.e3().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        long id = tester.target("/v1/e2")
                .queryParam("include", "e3s")
                .queryParam("exclude", "address", "e3s.name", "e3s.phoneNumber")
                .post("{\"e3s\":[1,8],\"name\":\"MM\"}")
                .wasCreated()
                .replaceId("XID")
                .bodyEquals(1, "{\"id\":XID,\"e3s\":[{\"id\":1},{\"id\":8}],\"name\":\"MM\"}")
                .getId();

        tester.e3().matcher().eq("e2_id", id).assertMatches(2);
    }

    @Test
    public void testPOST_Relate() {

        tester.e2().insertColumns("id_", "name").values(24, "xxx").exec();

        tester.target("/v1/e2/24/e3s")
                .post("{\"name\":\"zzz\"}")
                .wasCreated()
                .replaceId("XID")
                .bodyEquals(1, "{\"id\":XID,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().assertOneMatch();
    }

    @Test
    public void testPUT_ById() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(3, "zzz", null)
                .values(4, "aaa", 8)
                .values(5, "bbb", 8).exec();

        tester.target("/v1/e2/1")
                .queryParam("include", "e3s")
                .queryParam("exclude", "address", "name", "e3s.name", "e3s.phoneNumber")
                .put("{\"e3s\":[3,4,5]}")
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"e3s\":[{\"id\":3},{\"id\":4},{\"id\":5}]}");

        tester.e3().matcher().eq("e2_id", 1).assertMatches(3);
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

        tester.target("/v1/e2/1/e3s/9").delete().wasOk().bodyEquals("{\"success\":true}");

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

        tester.target("/v1/e2/1/e3s").delete().wasOk().bodyEquals("{\"success\":true}");

        // this delete is really "unrelate", so the records will be there, but their FKs will be set to null
        tester.e3().matcher().assertMatches(3);
        tester.e3().matcher().eq("e2_id", 1).assertNoMatches();
    }
}
